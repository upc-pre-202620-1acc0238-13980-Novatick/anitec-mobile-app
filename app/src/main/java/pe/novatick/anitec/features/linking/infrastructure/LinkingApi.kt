package pe.novatick.anitec.features.linking.infrastructure
import androidx.annotation.Keep
import pe.novatick.anitec.features.auth.infrastructure.Envelope
import retrofit2.Response
import retrofit2.http.*
@Keep data class InvitationDto(val invitationId: String?,val vetEmail: String?,val status: String?,val emailDelivery: String?,val sentAt: String?)
@Keep data class PendingDto(val invitationId: String?,val farmerName: String?,val sentDate: String?)
@Keep data class LinkDto(val linkId: String?,val farmerId: String?,val farmerName: String?,val vetId: String?,val vetName: String?,val linkedAt: String?,val status: String?)
@Keep data class LinksDto(val activeLinksCount: Long?,val links: List<LinkDto>?)
@Keep data class FarmDto(val farmId: String?,val farmName: String?,val animalsCount: Long?)
@Keep data class FarmerDto(val farmerId: String?,val farmerName: String?,val animalsCount: Long?,val farms: List<FarmDto>?)
@Keep data class FarmersDto(val activeLinksCount: Long?,val farmers: List<FarmerDto>?)
@Keep data class CapacityDto(val vetId: String?,val allowedRanchers: Int?,val activeLinks: Int?,val lastPlanRevision: Long?)
@Keep data class RevokeDto(val linkId: String?,val status: String?,val revokedAt: String?)
@Keep data class InviteRequest(val vetEmail: String)
@Keep data class RespondRequest(val action: String)
interface LinkingApi {
 @GET("visits") suspend fun visits(@Header("Authorization") jwt: String,@Query("from") from: String,@Query("to") to: String): Response<Envelope<List<HomeVisitDto>>>
 @GET("vet-links") suspend fun links(@Header("Authorization") jwt: String): Response<Envelope<LinksDto>>
 @POST("invitations") suspend fun invite(@Header("Authorization") jwt: String,@Header("Accept-Language") language: String,@Body body: InviteRequest): Response<Envelope<InvitationDto>>
 @GET("invitations/pending") suspend fun pending(@Header("Authorization") jwt: String): Response<Envelope<List<PendingDto>>>
 @PUT("invitations/{id}/respond") suspend fun respond(@Header("Authorization") jwt: String,@Path("id") id: String,@Body body: RespondRequest): Response<Envelope<InvitationDto>>
 @GET("vet-links/my-farmers") suspend fun farmers(@Header("Authorization") jwt: String): Response<Envelope<FarmersDto>>
 @GET("vet-links/capacity") suspend fun capacity(@Header("Authorization") jwt: String): Response<Envelope<CapacityDto>>
 @DELETE("vet-links/{id}") suspend fun revoke(@Header("Authorization") jwt: String,@Path("id") id: String): Response<Envelope<RevokeDto>>
}
@Keep data class HomeVisitDto(val id:String?,val animalId:String?,val animalName:String?,val farmerId:String?,val farmerName:String?,val type:String?,val scheduledAt:String?,val status:String?,val reason:String?)
