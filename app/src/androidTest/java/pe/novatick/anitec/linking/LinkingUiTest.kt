package pe.novatick.anitec.linking
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import pe.novatick.anitec.debug.DebugLinkingRepository
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.Locale
class LinkingUiTest {
 @get:Rule val compose=createComposeRule()
 private lateinit var repo: DebugLinkingRepository
 private fun open(language:String="es",scenario:String="content",screen:String="invite"){
  repo=DebugLinkingRepository(scenario)
  compose.setContent {
   val base=InstrumentationRegistry.getInstrumentation().targetContext
   val config=remember {Configuration(base.resources.configuration).apply {setLocale(Locale.forLanguageTag(language));fontScale=1f}}
   val localized=remember {base.createConfigurationContext(config)};val density=LocalDensity.current
   CompositionLocalProvider(LocalContext provides localized,LocalResources provides localized.resources,LocalConfiguration provides config,LocalDensity provides Density(density.density,1f)){
    AniTecTheme {
     val vm=remember {LinkingViewModel(repo,if(screen in listOf("invite","vets"))Role.GANADERO else Role.VETERINARIO)}
     DisposableEffect(vm){vm.refresh();onDispose{vm.close()}}
     when(screen){"invite"->InviteVetScreen(vm){};"vets"->RancherLinksScreen(vm){};"farmers"->VeterinarianFarmersScreen(vm,{},{})
      "detail"->LinkedFarmerDetailScreen(vm,DebugLinkingRepository.FARMER){};else->VeterinarianInvitationsScreen(vm){}}
    }
   }
  }
 }
 private fun invitation(language:String){
  open(language);compose.onNodeWithTag("link_send").performScrollTo().assertIsNotEnabled()
  compose.onNodeWithTag("link_email").performTextInput("vet@example.invalid")
  compose.onNodeWithTag("link_send").performScrollTo().assertIsEnabled().performClick()
  compose.onNodeWithTag("link_send").performScrollTo().assertIsNotEnabled()
  compose.onNodeWithText(if(language=="es")"Invitación registrada; el envío del correo no pudo confirmarse. La vinculación aún no está activa." else "Invitation saved; email delivery could not be confirmed. The link is not active yet.").assertExists()
 }
 @Test fun spanishValidInvitationConfirmedOnlyAsPending()=invitation("es")
 @Test fun englishUsesSameFormAndConfirmation()=invitation("en")
 @Test fun uncertainResultNeverEnablesAutomaticPost(){open("en","uncertain");compose.onNodeWithTag("link_email").performTextInput("vet@example.invalid");compose.onNodeWithTag("link_send").performScrollTo().performClick();compose.onNodeWithTag("link_send").performScrollTo().assertIsNotEnabled();compose.onNodeWithTag("link_email").assertTextContains("vet@example.invalid")}
 @Test fun acceptUpdatesPendingAndLinkedServerReadModels(){open(screen="invitations");compose.onNodeWithTag("link_accept_"+DebugLinkingRepository.INV).performScrollTo().performClick();compose.onNodeWithText("No hay invitaciones pendientes.").assertExists();Assert.assertTrue(repo.answeredAccepted);Assert.assertTrue(repo.accepted)}
 @Test fun rejectDoesNotCreateAnActiveLink(){open(screen="invitations");compose.onNodeWithTag("link_reject_"+DebugLinkingRepository.INV).performScrollTo().performClick();compose.onNodeWithText("No hay invitaciones pendientes.").assertExists();Assert.assertFalse(repo.answeredAccepted);Assert.assertTrue(repo.accepted)}
 @Test fun revokeRequiresConfirmationAndUpdatesList(){open(screen="vets");compose.onNodeWithText("Quitar").performClick();Assert.assertTrue(repo.accepted);compose.onNodeWithText("Cancelar").performClick();Assert.assertTrue(repo.accepted);compose.onNodeWithText("Quitar").performClick();compose.onAllNodesWithText("Quitar").onLast().performClick();compose.onNodeWithText("Aún no hay vinculaciones activas.").assertExists();Assert.assertFalse(repo.accepted)}
 @Test fun queryFailureDoesNotPretendEmpty(){open(scenario="error",screen="farmers");compose.onNodeWithText("No hay invitaciones pendientes.").assertDoesNotExist();compose.onNodeWithText("Aún no hay vinculaciones activas.").assertDoesNotExist()}
 @Test fun farmerSearchFiltersLocally(){open(screen="farmers");compose.onNodeWithTag("link_search").performTextInput("absent");compose.onNodeWithText("No hay coincidencias.").assertExists()}
 @Test fun detailShowsOnlyAuthorizedCountsAndNoUnlink(){open(screen="detail");compose.onNodeWithText("Fundo Los Álamos").assertExists();compose.onNodeWithText("Desvincular").assertDoesNotExist();compose.onNodeWithText("12 animales").assertExists()}
}
