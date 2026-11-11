package pe.novatick.anitec.debug
import kotlinx.coroutines.delay
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
/** Resettable fixtures: debug only, never attached to an authenticated HTTP session. */
class DebugLinkingRepository(val scenario: String="content"): LinkingRepository {
 companion object {const val FARMER="10000000-0000-4000-8000-000000000001";const val VET="20000000-0000-4000-8000-000000000001";const val INV="30000000-0000-4000-8000-000000000001";const val LINK="40000000-0000-4000-8000-000000000001"}
 var invited=false;var accepted=true;var pending=true;var answeredAccepted=false
 private suspend fun ready(){if(scenario=="loading")delay(60000);if(scenario=="error")throw AuthFailure(AuthProblem.NETWORK)}
 override suspend fun links():List<VetLink>{ready();return if(scenario=="empty")emptyList() else listOfNotNull(if(accepted) VetLink(LINK,FARMER,(if(scenario=="long")"Luis Alejandro Martínez de los Álamos y Rivera" else "Luis Martínez"),VET,(if(scenario=="long")"María Alejandra López de la Cruz y Fernández" else "María López"),"2026-11-11T12:00:00Z") else null,if(answeredAccepted) VetLink("40000000-0000-4000-8000-000000000002","10000000-0000-4000-8000-000000000002","Sara Valencia",VET,(if(scenario=="long")"María Alejandra López de la Cruz y Fernández" else "María López"),"2026-11-11T12:00:00Z") else null)}
 override suspend fun pending():List<PendingInvitation>{ready();return if(scenario=="empty" || !pending)emptyList() else listOf(PendingInvitation(INV,"Sara Valencia","2026-11-11"))}
 override suspend fun farmers():List<LinkedFarmer>{ready();return if(scenario=="empty")emptyList() else listOfNotNull(if(accepted) LinkedFarmer(FARMER,(if(scenario=="long")"Luis Alejandro Martínez de los Álamos y Rivera" else "Luis Martínez"),12,listOf(LinkedFarm("50000000-0000-4000-8000-000000000001","Fundo Los Álamos",12))) else null,if(answeredAccepted) LinkedFarmer("10000000-0000-4000-8000-000000000002","Sara Valencia",0,emptyList()) else null)}
 override suspend fun capacity():LinkCapacity{ready();return LinkCapacity(VET,3,(if(accepted)1 else 0)+(if(answeredAccepted)1 else 0),0)}
 override suspend fun invite(email: String,language: String):Invitation{if(invited)throw AuthFailure(AuthProblem.SERVER,409,"INVITATION_ALREADY_EXISTS");if(scenario=="uncertain")throw AuthFailure(AuthProblem.NETWORK);invited=true;return Invitation(INV,email,"PENDIENTE","UNCONFIRMED","2026-11-11T12:00:00Z")}
 override suspend fun respond(id: String,accept: Boolean):Invitation {pending=false;answeredAccepted=accept;return Invitation(id,"qa@example.invalid",if(accept)"ACEPTADA" else "RECHAZADA","UNCONFIRMED","2026-11-11T12:00:00Z")}
 override suspend fun revoke(id: String){accepted=false}
 override suspend fun farms(farmerId:String):List<Farm>{ready();if(scenario=="revoked")throw AuthFailure(AuthProblem.FORBIDDEN,403);return if(scenario=="empty")emptyList() else listOf(Farm("50000000-0000-4000-8000-000000000001","Fundo Los Álamos","Camino norte, junto al portón",farmerId,"2026-11-11T12:00:00Z"))}
 override suspend fun animals(farmerId:String):Inventory{ready();if(scenario=="revoked")throw AuthFailure(AuthProblem.FORBIDDEN,403);return if(scenario=="empty")Inventory(0,emptyList()) else Inventory(1,listOf(AnimalSummary("60000000-0000-4000-8000-000000000001","BOV-024","Luna","Bovino","70000000-0000-4000-8000-000000000001",AnimalStatus.ACTIVO,"50000000-0000-4000-8000-000000000001",farmerId,0)))}
 override suspend fun animal(farmerId:String,animalId:String):AnimalDetail{ready();if(scenario=="revoked")throw AuthFailure(AuthProblem.FORBIDDEN,403);return AnimalDetail(animalId,"BOV-024","Luna","Bovino","70000000-0000-4000-8000-000000000001",AnimalSex.HEMBRA,"Holstein","2021-03-12",AnimalStatus.ACTIVO,"50000000-0000-4000-8000-000000000001","Fundo Los Álamos",0,null)}

}
