package pe.novatick.anitec.debug
import kotlinx.coroutines.delay
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.auth.domain.*
/** Resettable fixtures: debug only, never attached to an authenticated HTTP session. */
class DebugLinkingRepository(val scenario: String="content"): LinkingRepository {
 companion object {const val FARMER="10000000-0000-4000-8000-000000000001";const val VET="20000000-0000-4000-8000-000000000001";const val INV="30000000-0000-4000-8000-000000000001";const val LINK="40000000-0000-4000-8000-000000000001"}
 var invited=false;var accepted=true;var pending=true
 private suspend fun ready(){if(scenario=="loading")delay(60000);if(scenario=="error")throw AuthFailure(AuthProblem.NETWORK)}
 override suspend fun links():List<VetLink>{ready();return if(scenario=="empty" || !accepted)emptyList() else listOf(VetLink(LINK,FARMER,"Luis Martínez",VET,"María López","2026-11-11T12:00:00Z"))}
 override suspend fun pending():List<PendingInvitation>{ready();return if(scenario=="empty" || !pending)emptyList() else listOf(PendingInvitation(INV,"Sara Valencia","2026-11-11"))}
 override suspend fun farmers():List<LinkedFarmer>{ready();return if(scenario=="empty" || !accepted)emptyList() else listOf(LinkedFarmer(FARMER,"Luis Martínez",12,listOf(LinkedFarm("50000000-0000-4000-8000-000000000001","Fundo Los Álamos",12))))}
 override suspend fun capacity():LinkCapacity{ready();return LinkCapacity(VET,3,if(accepted)1 else 0,0)}
 override suspend fun invite(email: String,language: String):Invitation{if(invited)throw AuthFailure(AuthProblem.SERVER,409,"INVITATION_ALREADY_EXISTS");if(scenario=="uncertain")throw AuthFailure(AuthProblem.NETWORK);invited=true;return Invitation(INV,email,"PENDIENTE","UNCONFIRMED","2026-11-11T12:00:00Z")}
 override suspend fun respond(id: String,accept: Boolean):Invitation {pending=false;accepted=accept;return Invitation(id,"qa@example.invalid",if(accept)"ACEPTADA" else "RECHAZADA","UNCONFIRMED","2026-11-11T12:00:00Z")}
 override suspend fun revoke(id: String){accepted=false}
}
