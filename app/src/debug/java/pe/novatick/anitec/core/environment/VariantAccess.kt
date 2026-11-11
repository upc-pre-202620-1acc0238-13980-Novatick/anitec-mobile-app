package pe.novatick.anitec.core.environment
import androidx.compose.runtime.Composable
import okhttp3.OkHttpClient
/** No temporary credential UI, storage or header mechanism in these variants. */
internal object VariantAccess {
    fun isReady()=true
    fun configure(builder: OkHttpClient.Builder)=builder
    @Composable fun Gate(refresh: suspend ()->Unit,content: @Composable ()->Unit) {content()}
}
