package pe.novatick.anitec.core.network
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import retrofit2.Response
/** Infrastructure-only port; credentials never reach presentation. */
interface AuthorizedRequests {
    suspend fun <T> execute(request: suspend (String)->Response<Envelope<T>>): T
}
