package pe.novatick.anitec.features.linking.domain

data class Invitation(val id: String,val email: String,val status: String,val delivery: String,val sentAt: String)
data class PendingInvitation(val id: String,val farmerName: String,val sentDate: String)
data class VetLink(val id: String,val farmerId: String,val farmerName: String,val vetId: String,val vetName: String,val linkedAt: String)
data class LinkedFarm(val id: String,val name: String,val animals: Long)
data class LinkedFarmer(val id: String,val name: String,val animals: Long,val farms: List<LinkedFarm>)
data class LinkCapacity(val vetId: String,val allowed: Int,val active: Int,val revision: Long)
interface LinkingRepository {
    suspend fun observations(farmerId: String,animalId: String): List<pe.novatick.anitec.features.livestock.domain.AnimalObservation> = throw pe.novatick.anitec.features.auth.domain.AuthFailure(pe.novatick.anitec.features.auth.domain.AuthProblem.FORBIDDEN)
    suspend fun farms(farmerId: String): List<pe.novatick.anitec.features.livestock.domain.Farm> = throw pe.novatick.anitec.features.auth.domain.AuthFailure(pe.novatick.anitec.features.auth.domain.AuthProblem.FORBIDDEN)
    suspend fun animals(farmerId: String): pe.novatick.anitec.features.livestock.domain.Inventory = throw pe.novatick.anitec.features.auth.domain.AuthFailure(pe.novatick.anitec.features.auth.domain.AuthProblem.FORBIDDEN)
    suspend fun animal(farmerId: String,animalId: String): pe.novatick.anitec.features.livestock.domain.AnimalDetail = throw pe.novatick.anitec.features.auth.domain.AuthFailure(pe.novatick.anitec.features.auth.domain.AuthProblem.FORBIDDEN)
    suspend fun links(): List<VetLink>
    suspend fun invite(email: String,language: String): Invitation
    suspend fun pending(): List<PendingInvitation>
    suspend fun respond(id: String,accept: Boolean): Invitation
    suspend fun farmers(): List<LinkedFarmer>
    suspend fun capacity(): LinkCapacity
    suspend fun revoke(id: String)
}
fun validVetEmail(value: String): Boolean = value.trim().let {it.length<=150 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(it)}
