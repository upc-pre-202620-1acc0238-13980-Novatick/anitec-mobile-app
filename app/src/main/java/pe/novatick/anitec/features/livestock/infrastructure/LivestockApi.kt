package pe.novatick.anitec.features.livestock.infrastructure
import androidx.annotation.Keep
import retrofit2.Response
import retrofit2.http.*
import pe.novatick.anitec.features.auth.infrastructure.Envelope
@Keep data class FarmDto(val id: String?,val name: String?,val location: String?,val ownerId: String?,val createdAt: String?)
@Keep data class SpeciesDto(val id: String?,val name: String?,val description: String?,val photoUrl: String?)
@Keep data class AnimalDto(val id: String?,val code: String?,val name: String?,val species: String?,val speciesId: String?,
    val status: String?,val farmId: String?,val ownerId: String?,val version: Long?)
@Keep data class InventoryDto(val totalActive: Long?,val content: List<AnimalDto>?)
@Keep data class CapacityDto(val ownerId: String?,val allowedAnimals: Long?,val activeAnimals: Long?,val lastPlanRevision: Long?)
@Keep class CreateFarmRequest(val name: String,val location: String?)
interface LivestockApi {
    @GET("farms") suspend fun farms(@Header("Authorization") bearer: String): Response<Envelope<List<FarmDto>>>
    @GET("farms/{id}") suspend fun farm(@Header("Authorization") bearer: String,@Path("id") id: String): Response<Envelope<FarmDto>>
    @POST("farms") suspend fun createFarm(@Header("Authorization") bearer: String,@Body body: CreateFarmRequest): Response<Envelope<FarmDto>>
    @GET("species") suspend fun species(@Header("Authorization") bearer: String): Response<Envelope<List<SpeciesDto>>>
    @GET("animals") suspend fun inventory(@Header("Authorization") bearer: String,@Query("search") search: String?,@Query("species") species: String?,
        @Query("status") status: String?,@Query("farmId") farmId: String?): Response<Envelope<InventoryDto>>
    @GET("farms/{id}/animals") suspend fun farmAnimals(@Header("Authorization") bearer: String,@Path("id") id: String,
        @Query("search") search: String?,@Query("species") species: String?,@Query("status") status: String?): Response<Envelope<InventoryDto>>
    @GET("animals/capacity") suspend fun capacity(@Header("Authorization") bearer: String): Response<Envelope<CapacityDto>>
}
