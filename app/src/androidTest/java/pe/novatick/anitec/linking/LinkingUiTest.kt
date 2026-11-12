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
      "home"->VeterinaryHome(vm,Profile(DebugLinkingRepository.VET,"qa@example.invalid","María","López",Role.VETERINARIO),{},{},{})
      "observations"->LinkedObservationsScreen(vm,DebugLinkingRepository.FARMER,"60000000-0000-4000-8000-000000000001"){}
      "account"->pe.novatick.anitec.ui.screens.home.RancherProfileScreen("María Alejandra López de la Cruz y Fernández","qa@example.invalid",androidx.compose.ui.res.stringResource(pe.novatick.anitec.R.string.lk_veterinarian_role))
      "detail"->LinkedFarmerDetailScreen(vm,DebugLinkingRepository.FARMER,{},{});else->VeterinarianInvitationsScreen(vm){}}
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
 @Test fun detailShowsActualLocationAndAnimalsEntryWithoutUnlink(){open(screen="detail");compose.onNodeWithText("Fundo Los Álamos").assertExists();compose.onNodeWithText("Desvincular").assertDoesNotExist();compose.onNodeWithText("Camino norte, junto al portón").assertExists();compose.onNodeWithTag("linked_view_animals").assertExists()}
 @Test fun veterinarianCanNavigateFromFarmerToReadOnlyAnimalAndBack(){
  val repo=DebugLinkingRepository()
  compose.setContent {AniTecTheme {VeterinaryLinkingShell(repo,Profile(DebugLinkingRepository.VET,"qa@example.invalid","María","López",Role.VETERINARIO)) {}}}
  compose.onNodeWithText("Luis Martínez").performScrollTo().performClick()
  compose.onNodeWithTag("linked_view_animals").performScrollTo().performClick()
  compose.onNodeWithText("Luna").performScrollTo().performClick()
  compose.onNodeWithTag("animal_header_title").assertTextContains("Luna")
  compose.onNodeWithTag("vet_observations_all").performScrollTo().performClick()
  compose.onNodeWithText("Se adapta bien al lote y mantiene buen apetito.").assertExists()
  compose.onNodeWithText("Desde ayer come un poco menos, pero sin otros síntomas visibles.").assertExists()
  compose.onNodeWithText("Agregar observación").assertDoesNotExist()
  compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(pe.novatick.anitec.R.string.animal_back)).performClick()
  compose.onNodeWithTag("animal_edit").assertDoesNotExist();compose.onNodeWithTag("animal_deactivate").assertDoesNotExist()
  compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(pe.novatick.anitec.R.string.animal_more)).assertDoesNotExist()
  compose.onNodeWithContentDescription(InstrumentationRegistry.getInstrumentation().targetContext.getString(pe.novatick.anitec.R.string.animal_back)).performClick()
  compose.onNodeWithText("Luna").assertExists()
 }

 @Test fun ownerStillHasEditAndDeactivateActions(){
  compose.setContent {pe.novatick.anitec.livestock.LivestockLocaleFixture("es",pe.novatick.anitec.livestock.LivestockReviewRepository())}
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  compose.onNodeWithText(context.getString(pe.novatick.anitec.R.string.ls_tab_cattle)).performClick()
  compose.onNodeWithText("Luna").performScrollTo().performClick()
  compose.onNodeWithContentDescription(context.getString(pe.novatick.anitec.R.string.animal_more)).performClick()
  compose.onNodeWithTag("animal_edit").assertIsEnabled();compose.onNodeWithTag("animal_deactivate").assertIsEnabled()
 }
 @Test fun veterinaryHomeUsesRealCountAndNoVisitStatistics(){open(screen="home");compose.onNodeWithTag("home_link_count",useUnmergedTree=true).assertTextContains("1");compose.onNodeWithTag("home_pending").assertExists();compose.onNodeWithText("Aquí aparecerán tus próximas visitas").assertExists();compose.onNodeWithText("4 visitas hoy").assertDoesNotExist()}
 @Test fun emptyEnglishHomeDoesNotInventPendingInvitations(){open("en","empty","home");compose.onNodeWithTag("home_link_count",useUnmergedTree=true).assertTextContains("0");compose.onNodeWithTag("home_pending").assertDoesNotExist();compose.onNodeWithText("Your upcoming visits will appear here").assertExists()}
 @Test fun veterinaryObservationsShowServerMetadataWithoutMutationControls(){open(screen="observations");compose.onNodeWithText("Luis Martínez · 10 nov 2026").assertExists();compose.onNodeWithText("Agregar observación").assertDoesNotExist();compose.onNodeWithTag("observation_save").assertDoesNotExist()}
 @Test fun deniedObservationsAreNotPresentedAsEmpty(){open(scenario="observationError",screen="observations");compose.onNodeWithText("Todavía no hay observaciones.").assertDoesNotExist();compose.onNodeWithText("No se pudieron cargar las observaciones.").assertExists();compose.onNodeWithText("Reintentar").assertExists()}
 @Test fun englishAccountLocalizesRoleAndScrollsWithLongName(){open("en",screen="account");compose.onNodeWithText("María Alejandra López de la Cruz y Fernández").assertIsDisplayed();compose.onNodeWithText("Veterinarian").assertExists();compose.onNodeWithText("Help and legal").performScrollTo().assertIsDisplayed();compose.onNodeWithText("Sign out").performScrollTo().assertIsDisplayed()}
 @Test fun rejectionConfirmationDisappearsWithoutLeavingPermanentSpace(){open(screen="invitations");compose.onNodeWithTag("link_reject_"+DebugLinkingRepository.INV).performScrollTo().performClick();compose.onNodeWithText("Invitación rechazada.").assertExists();compose.mainClock.advanceTimeBy(8000);compose.waitForIdle();compose.onNodeWithText("Invitación rechazada.").assertDoesNotExist();compose.onNodeWithText("No hay invitaciones pendientes.").assertExists()}
 @Test fun removeModalIsCenteredAboveFullScreenScrim(){open(screen="vets");compose.onNodeWithText("Quitar").performClick();val dialog=compose.onNodeWithTag("remove_vet_dialog").assertIsDisplayed().fetchSemanticsNode().boundsInRoot;val scrim=compose.onNodeWithTag("remove_vet_scrim").fetchSemanticsNode().boundsInRoot;Assert.assertTrue(kotlin.math.abs(dialog.center.x-scrim.center.x)<4);Assert.assertTrue(kotlin.math.abs(dialog.center.y-scrim.center.y)<80);compose.onNodeWithTag("remove_vet_confirm").assertIsDisplayed();compose.onNodeWithText("Cancelar").assertIsDisplayed()}

}
