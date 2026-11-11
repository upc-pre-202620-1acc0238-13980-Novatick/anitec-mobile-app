package pe.novatick.anitec.core.environment

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Interceptor
import okhttp3.Request
import okio.ByteString.Companion.decodeBase64
import java.util.concurrent.atomic.AtomicReference

internal object MemoryStagingKey {
    private val key=AtomicReference<String?>(null)
    private val configured=MutableStateFlow(false)
    val ready=configured.asStateFlow()
    fun read(): String?=key.get()
    fun valid(value: String)=value.matches(Regex("[A-Za-z0-9+/]{43}=")) && value.decodeBase64()?.size==32 && value.decodeBase64()?.base64()==value
    fun set(value: String) {require(valid(value));key.set(value);configured.value=true}
    fun clear() {key.set(null);configured.value=false}
}

internal object StagingHeader {
    const val NAME="X-ANITEC-Staging-Key"
    fun prepare(request: Request,key: String?): Request {
        val url=request.url
        val builder=request.newBuilder().removeHeader(NAME)
        if(url.scheme=="https" && url.host=="anitec-staging.onrender.com" && url.port==443 &&
            url.username.isEmpty() && url.password.isEmpty() && url.encodedPath.startsWith("/api/v1/") &&
            key!=null && MemoryStagingKey.valid(key)) builder.header(NAME,key)
        return builder.build()
    }
    val interceptor=Interceptor {chain->chain.proceed(prepare(chain.request(),MemoryStagingKey.read()))}
}
