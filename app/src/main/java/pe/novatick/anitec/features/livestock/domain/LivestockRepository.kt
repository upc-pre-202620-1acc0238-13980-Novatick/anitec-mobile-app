package pe.novatick.anitec.features.livestock.domain

enum class AnimalStatus { ACTIVO, INACTIVO, VENDIDO, FALLECIDO }
data class Farm(val id: String,val name: String,val location: String?,val ownerId: String,val createdAt: String)
data class Species(val id: String,val name: String,val description: String?,val photoUrl: String?)
data class AnimalSummary(val id: String,val code: String,val name: String?,val species: String,val speciesId: String,
    val status: AnimalStatus,val farmId: String,val ownerId: String,val version: Long)
data class Inventory(val totalActive: Long,val content: List<AnimalSummary>)
data class Capacity(val ownerId: String,val allowedAnimals: Long,val activeAnimals: Long,val lastPlanRevision: Long) {
    val remaining: Long get()=(allowedAnimals-activeAnimals).coerceAtLeast(0)
}
data class InventoryFilter(val search: String="",val species: String?=null,val status: AnimalStatus?=AnimalStatus.ACTIVO,val farmId: String?=null)
interface LivestockRepository {
    suspend fun farms(): List<Farm>
    suspend fun farm(id: String): Farm
    suspend fun createFarm(name: String,location: String?): Farm
    suspend fun species(): List<Species>
    suspend fun inventory(filter: InventoryFilter): Inventory
    suspend fun capacity(): Capacity
}
