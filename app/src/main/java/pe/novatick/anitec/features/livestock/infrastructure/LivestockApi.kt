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
@Keep data class AnimalDataRequest(val farmId: String,val code: String?,val speciesId: String,val sex: String,
    val name: String?,val breed: String?,val birthDate: String?,val photoUrl: String?)
@Keep data class AnimalDetailDto(val id: String?,val code: String?,val name: String?,val species: String?,val sex: String?,
    val breed: String?,val birthDate: String?,val status: String?,val farmId: String?,val farmName: String?,val version: Long?,
    val speciesId: String?,val photoUrl: String?)
@Keep data class AnimalUpdateRequest(val code: String,val speciesId: String,val sex: String,val name: String?,
    val breed: String?,val birthDate: String?,val photoUrl: String?,val version: Long)
@Keep data class AnimalStatusDto(val id: String?,val status: String?)
interface LivestockApi {
    @PUT("animals/{id}") suspend fun updateAnimal(@Header("Authorization") bearer: String,@Path("id") id: String,@Body body: AnimalUpdateRequest): Response<Envelope<AnimalDto>>
    @DELETE("animals/{id}") suspend fun deactivateAnimal(@Header("Authorization") bearer: String,@Path("id") id: String): Response<Envelope<AnimalStatusDto>>
    @POST("animals") suspend fun createAnimal(@Header("Authorization") bearer: String,@Body body: AnimalDataRequest): Response<Envelope<AnimalDto>>
    @GET("animals/{id}") suspend fun animal(@Header("Authorization") bearer: String,@Path("id") id: String): Response<Envelope<AnimalDetailDto>>
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
