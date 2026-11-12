package pe.novatick.anitec.features.auth.infrastructure

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.novatick.anitec.features.auth.domain.*
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class RegistrationRepositoryImpl(private val api: AuthApi, private val now: ()->Long = System::currentTimeMillis) : RegistrationRepository {
    private fun invalid(): Nothing = throw RegistrationFailure(code="INVALID_RESPONSE")
    private fun language(value: String) = if(value=="en") "en" else "es"
    private fun instant(raw: String?): Long? {
        if(raw==null) return null
        val match=Regex("""(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d{1,9}))?Z""").matchEntire(raw) ?: return null
        val text=match.groupValues[1]+"."+match.groupValues[2].padEnd(3,'0').take(3)+"Z"
        return try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US).apply {
            timeZone=TimeZone.getTimeZone("UTC"); isLenient=false
        }.parse(text)?.time } catch(_: Exception) { null }
    }
    private fun <T> envelope(response: Response<Envelope<T>>, expected: Int): Envelope<T> {
        if(!response.isSuccessful) {
            val e=try { response.errorBody()?.use { Gson().fromJson(it.string(),ApiError::class.java) } } catch(_: Exception) { null }
            val seconds=response.headers()["Retry-After"]?.toLongOrNull()?.takeIf { it in 0..31536000 }
            val fromHeader=seconds?.let { now()+it*1000 }
            val retry=e?.details.orEmpty().filter { it.field=="retryAt" }.mapNotNull { instant(it.error) }.maxOrNull()
            throw RegistrationFailure(status=response.code(),code=e?.errorCode,
                fields=e?.details.orEmpty().mapNotNull { it.field }.filter { it in listOf("name","lastName","email","password","role","code") },
                retryAt=listOfNotNull(fromHeader,retry).maxOrNull())
        }
        if(response.code()!=expected) invalid()
        return (response.body() ?: invalid()).also { if(it.success!=true || it.message==null || it.timestamp==null) invalid() }
    }
    private suspend fun <T> safe(action: suspend ()->T): T = withContext(Dispatchers.IO) {
        try { action() } catch(e: CancellationException) { throw e } catch(e: RegistrationFailure) { throw e }
        catch(_: com.google.gson.stream.MalformedJsonException) { throw RegistrationFailure(code="INVALID_RESPONSE") }
        catch(_: java.io.EOFException) { throw RegistrationFailure(code="INVALID_RESPONSE") }
        catch(_: IOException) { throw RegistrationFailure(code="NETWORK_UNCERTAIN") }
        catch(_: Exception) { throw RegistrationFailure(code="INVALID_RESPONSE") }
    }
    override suspend fun register(form: RegistrationForm, language: String) = safe {
        RegistrationValidation.validate(form)
        val d=envelope(api.register(RegisterRequest(form.name.trim(),form.lastName.trim(),form.email.trim(),form.password,form.role.name),language(language)),201).data ?: invalid()
        val id=d.accountId ?: invalid(); UUID.fromString(id)
        val email=d.email ?: invalid()
        if(!RegistrationValidation.email(email) || !email.equals(form.email.trim(),true) || d.emailDelivery!="UNCONFIRMED") invalid()
        RegistrationResult(id,email)
    }
    override suspend fun verify(email: String, code: String, password: String?) { safe {
        if(!RegistrationValidation.email(email) || !RegistrationValidation.otp(code)) throw RegistrationFailure(RegistrationIssue.OTP)
        if(password!=null && password.length>256) throw RegistrationFailure(RegistrationIssue.CONTEXT)
        envelope(api.verify(VerifyRequest(email,code,password)),200)
    } }
    override suspend fun resend(email: String, password: String?, language: String) = safe {
        if(!RegistrationValidation.email(email) || (password!=null && password.length>256)) throw RegistrationFailure(RegistrationIssue.CONTEXT)
        val data=envelope(api.resend(ResendRequest(email,password),language(language)),202).data
        if(data==null) ResendResult(false) else {
            if(!email.equals(data.email,true) || data.emailDelivery!="UNCONFIRMED" || data.remainingResends !in 0..5) invalid()
            val next=instant(data.nextResendAt) ?: invalid()
            ResendResult(true,data.remainingResends,next)
        }
    }
}
