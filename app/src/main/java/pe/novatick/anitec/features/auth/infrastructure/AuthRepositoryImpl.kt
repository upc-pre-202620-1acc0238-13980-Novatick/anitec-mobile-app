package pe.novatick.anitec.features.auth.infrastructure

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withContext
import pe.novatick.anitec.core.session.*
import pe.novatick.anitec.features.auth.domain.*
import retrofit2.Response
import java.io.IOException
import java.util.UUID

class AuthRepositoryImpl(private val api: AuthApi, private val store: SessionStore,
                         private val now: () -> Long = System::currentTimeMillis) : AuthRepository, pe.novatick.anitec.core.network.AuthorizedRequests {
    private val lock = Any()
    private val rotation = Mutex()
    private var epoch = 0L
    private var session: SessionRecord? = null
    private class Closing(var token: String?) {
        var local = InvalidationResult(Absence.UNKNOWN, Absence.UNKNOWN)
        var remote = false
        val network = Mutex()
    }
    private var closing: Closing? = null
    private fun invalidateSafely() = try { store.invalidate() }
        catch (_: Exception) { InvalidationResult(Absence.UNKNOWN, Absence.UNKNOWN) }
    private val identity = MutableStateFlow<Profile?>(null)
    override val profile = identity.asStateFlow()
    private val gson = Gson()
    private fun invalid(): Nothing = throw AuthFailure(AuthProblem.INVALID_RESPONSE)
    private fun text(s: String?): String = s?.takeIf { it.isNotBlank() } ?: invalid()
    private fun uuid(s: String?): String = text(s).also { try { UUID.fromString(it) } catch (_: Exception) { invalid() } }
    private fun role(s: String?): Role = Role.entries.firstOrNull { it.name == s } ?: invalid()
    private fun expiry(seconds: Long?): Long {
        if (seconds == null || seconds <= 0 || seconds > 604800) invalid()
        return now() + seconds * 1000
    }
    private fun <T> body(r: Response<Envelope<T>>): T {
        if (!r.isSuccessful) {
            val error = try { r.errorBody()?.use { gson.fromJson(it.string(), ApiError::class.java) } } catch (_: Exception) { null }
            val problem = when(r.code()) { 401 -> AuthProblem.UNAUTHORIZED; 403 -> AuthProblem.FORBIDDEN; else -> AuthProblem.SERVER }
            throw AuthFailure(problem,r.code(),error?.errorCode,error?.details.orEmpty().mapNotNull { f ->
                if(f.field != null && f.error != null) f.field to f.error else null
            })
        }
        val e = r.body() ?: invalid()
        if (e.success != true || e.message == null || e.timestamp == null) invalid()
        return e.data ?: invalid()
    }
    private suspend fun <T> safe(block: suspend () -> T): T = try { block() }
        catch(e: CancellationException) { throw e }
        catch(e: AuthFailure) { throw e }
        catch(_: IOException) { throw AuthFailure(AuthProblem.NETWORK) }
        catch(_: Exception) { throw AuthFailure(AuthProblem.INVALID_RESPONSE) }
    private fun persist(s: SessionRecord) {
        try { if(s.persistent) store.write(s) else store.clear() }
        catch(_: Exception) { throw AuthFailure(AuthProblem.STORAGE) }
    }
    private fun checkEpoch(e: Long) { if(epoch != e) throw CancellationException("Session changed") }
    private fun clearLocked() { session=null; identity.value=null; store.clear() }
    private suspend fun authorized(s: SessionRecord): Profile {
        val p = body(api.me("Bearer ${s.access}"))
        val result = Profile(uuid(p.id),text(p.email),text(p.name),text(p.lastName),role(p.role))
        if(result.id != s.accountId || result.role.name != s.role || p.emailVerified != true) invalid()
        return result
    }
    override suspend fun login(email: String, password: String, remember: Boolean) = withContext(Dispatchers.IO) { safe {
        val e = synchronized(lock) {
            closing?.let { if(!it.local.protected) {
                it.local = invalidateSafely()
                if(!it.local.protected) throw AuthFailure(AuthProblem.STORAGE)
            } }
            epoch++; clearLocked(); closing=null; epoch
        }
        val d = try { body(api.login(LoginRequest(email.trim(),password))) }
        catch(f: AuthFailure) { if(f.status==401) throw AuthFailure(AuthProblem.CREDENTIALS,401,f.code,f.fields); throw f }
        val s = SessionRecord(uuid(d.accountId),text(d.email),role(d.role).name,text(d.accessToken),text(d.refreshToken),expiry(d.expiresIn),remember)
        val p = authorized(s)
        synchronized(lock) { checkEpoch(e); persist(s); session=s; identity.value=p }
    } }
    override suspend fun restore() = withContext(Dispatchers.IO) { safe {
        val e: Long
        var s: SessionRecord
        synchronized(lock) {
            if(closing != null) return@safe
            e=epoch
            s=session ?: try { store.read() } catch(_: Exception) { store.clear(); throw AuthFailure(AuthProblem.STORAGE) } ?: return@safe
            uuid(s.accountId); text(s.email); role(s.role); text(s.access); text(s.refresh)
            session=s
        }
        try {
            if(s.pendingId != null || s.expiresAt <= now()) { rotate(s,e); s=synchronized(lock) { checkEpoch(e); session ?: invalid() } }
            val p = try { authorized(s) } catch(f: AuthFailure) {
                if(f.status!=401 || f.code!="ACCESS_TOKEN_EXPIRED") throw f
                rotate(s,e); authorized(synchronized(lock) { checkEpoch(e); session ?: invalid() })
            }
            synchronized(lock) { checkEpoch(e); identity.value=p }
        } catch(f: AuthFailure) {
            if(f.problem==AuthProblem.UNAUTHORIZED || f.problem==AuthProblem.RELOGIN || f.problem==AuthProblem.INVALID_RESPONSE) {
                synchronized(lock) { if(epoch==e) { epoch++; clearLocked() } }
            }
            throw f
        }
    } }
    override suspend fun refresh() = withContext(Dispatchers.IO) { safe {
        val (s,e)=synchronized(lock) { (session ?: throw AuthFailure(AuthProblem.RELOGIN)) to epoch }
        try { rotate(s,e) } catch(f: AuthFailure) {
            if(f.problem==AuthProblem.UNAUTHORIZED || f.problem==AuthProblem.RELOGIN) synchronized(lock) { if(epoch==e) { epoch++; clearLocked() } }
            throw f
        }
    } }
    private suspend fun rotate(expected: SessionRecord, e: Long) = rotation.withLock {
        val pending = synchronized(lock) {
            checkEpoch(e)
            val current=session ?: throw AuthFailure(AuthProblem.RELOGIN)
            if(current !== expected && current.pendingId==null) return@withLock
            val p = if(current.pendingId!=null) current else current.pending(UUID.randomUUID().toString(),now())
            val started=p.pendingSince ?: throw AuthFailure(AuthProblem.RELOGIN)
            if(now()<started || now()-started>=60000) throw AuthFailure(AuthProblem.RELOGIN)
            persist(p); session=p; p
        }
        // Pending request remains durable on a lost response or REFRESH_CONFIRMATION_PENDING.
        val d=body(api.refresh(RefreshRequest(pending.refresh,pending.pendingId!!)))
        val updated=SessionRecord(pending.accountId,pending.email,pending.role,text(d.accessToken),text(d.refreshToken),expiry(d.expiresIn),pending.persistent,generation=pending.generation)
        synchronized(lock) { checkEpoch(e); persist(updated); session=updated }
    }
    override suspend fun <T> execute(request: suspend (String)->Response<Envelope<T>>): T = withContext(Dispatchers.IO) { safe {
        val (initial,e)=synchronized(lock) {
            if(closing!=null || identity.value==null) throw AuthFailure(AuthProblem.RELOGIN)
            (session ?: throw AuthFailure(AuthProblem.RELOGIN)) to epoch
        }
        try {
            var current=initial
            if(current.pendingId!=null || current.expiresAt<=now()) {
                rotate(current,e)
                current=synchronized(lock) { checkEpoch(e);session ?: throw AuthFailure(AuthProblem.RELOGIN) }
            }
            val value=try { body(request("Bearer ${current.access}")) } catch(f: AuthFailure) {
                synchronized(lock) { checkEpoch(e) }
                if(f.status!=401 || f.code!="ACCESS_TOKEN_EXPIRED") throw f
                rotate(current,e)
                val renewed=synchronized(lock) { checkEpoch(e);session ?: throw AuthFailure(AuthProblem.RELOGIN) }
                body(request("Bearer ${renewed.access}"))
            }
            synchronized(lock) { checkEpoch(e);value }
        } catch(f: AuthFailure) {
            synchronized(lock) {
                checkEpoch(e)
                if(f.status==401 || f.problem==AuthProblem.RELOGIN) logoutLocally()
            }
            throw f
        }
    } }
    override fun logoutLocally(): suspend () -> LogoutResult {
        val target = synchronized(lock) {
            val target = closing ?: Closing(session?.refresh).also { closing=it }
            epoch++; session=null; identity.value=null
            target.local=invalidateSafely()
            target
        }
        return { target.network.withLock { withContext(Dispatchers.IO) {
            val token = synchronized(lock) { target.token }
            val confirmed = if(target.remote) true else if(token==null) false else try {
                withTimeout(15000) {
                    val r=api.logout(LogoutRequest(token))
                    r.isSuccessful && r.body()?.let { it.success==true && it.message!=null && it.timestamp!=null }==true
                }
            } catch(e: kotlinx.coroutines.TimeoutCancellationException) { false }
              catch(e: CancellationException) { throw e } catch(_: Exception) { false }
            synchronized(lock) {
                target.remote=confirmed
                if(confirmed) target.token=null
                LogoutResult(target.local,confirmed)
            }
        } } }
    }
}
