package pe.novatick.anitec.features.livestock.infrastructure
import pe.novatick.anitec.core.network.AuthorizedRequests
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale

class LivestockRepositoryImpl(private val api: LivestockApi,private val requests: AuthorizedRequests,private val owner: ()->String?) : LivestockRepository {
    private suspend fun <T> mapped(block: suspend ()->T): T = try { block() }
        catch(e: kotlinx.coroutines.CancellationException) { throw e }
        catch(e: AuthFailure) { throw e }
        catch(_: Exception) { invalid() }
    private fun <T> expected(response: retrofit2.Response<T>,status: Int=200): retrofit2.Response<T> {
        if(response.isSuccessful && response.code()!=status) invalid()
        return response
    }
    private fun invalid(): Nothing=throw AuthFailure(AuthProblem.INVALID_RESPONSE)
    private fun id(s: String?): String { if(s==null || runCatching { UUID.fromString(s).toString()==s.lowercase() }.getOrDefault(false).not()) invalid();return s }
    private fun text(s: String?)=s?.takeIf { it.isNotBlank() } ?: invalid()
    private fun count(n: Long?)=n?.takeIf { it>=0 } ?: invalid()
    private fun owned(s: String?)=id(s).also { if(it!=owner()) invalid() }
    private fun farm(d: FarmDto)=Farm(id(d.id),text(d.name),d.location,owned(d.ownerId),text(d.createdAt).also {
        // Validate UTC timestamps without depending on API 26 java.time.
        val raw=it.replace(Regex("\\.\\d+Z$"),"Z")
        val parser=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).apply { isLenient=false }
        val position=java.text.ParsePosition(0)
        if(parser.parse(raw,position)==null || position.index!=raw.length) invalid()
    })
    override suspend fun farms()=mapped { requests.execute { expected(api.farms(it)) }.map(::farm) }
    override suspend fun farm(id: String)=mapped { farm(requests.execute { expected(api.farm(it,this.id(id))) }).also { if(it.id!=id) invalid() } }
    override suspend fun createFarm(name: String,location: String?): Farm {
        if(name.isBlank() || name.length>150 || (location?.length ?: 0)>255) throw AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR")
        return mapped { farm(requests.execute { expected(api.createFarm(it,CreateFarmRequest(name.trim(),location?.trim()?.takeIf(String::isNotEmpty))),201) }) }
    }
    override suspend fun species()=mapped { requests.execute { expected(api.species(it)) }.map { Species(id(it.id),text(it.name),it.description,it.photoUrl) } }
    override suspend fun capacity()=mapped { requests.execute { expected(api.capacity(it)) }.let { Capacity(owned(it.ownerId),count(it.allowedAnimals),count(it.activeAnimals),count(it.lastPlanRevision)) } }
    override suspend fun inventory(filter: InventoryFilter): Inventory = mapped {
        filter.species?.let { id(it) };filter.farmId?.let { id(it) }
        val d=requests.execute { token -> if(filter.farmId==null) expected(api.inventory(token,filter.search.takeIf(String::isNotBlank),filter.species,filter.status?.name,null))
            else expected(api.farmAnimals(token,filter.farmId,filter.search.takeIf(String::isNotBlank),filter.species,filter.status?.name)) }
        Inventory(count(d.totalActive),(d.content ?: invalid()).map { a ->
            AnimalSummary(id(a.id),text(a.code),a.name,text(a.species),id(a.speciesId),AnimalStatus.entries.firstOrNull { it.name==a.status } ?: invalid(),
                id(a.farmId).also { if(filter.farmId!=null && it!=filter.farmId) invalid() },owned(a.ownerId),count(a.version))
        })
    }
}
