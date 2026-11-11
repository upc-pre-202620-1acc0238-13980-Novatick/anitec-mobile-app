package pe.novatick.anitec.debug
import android.os.Bundle
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.components.*
import pe.novatick.anitec.ui.navigation.*
import pe.novatick.anitec.features.auth.domain.Role
import pe.novatick.anitec.features.linking.presentation.*
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.Locale
/** Explicit offline visual fixture harness, absent from Alpha and release. */
class LinkingReviewActivity: ComponentActivity(){
 override fun attachBaseContext(base: Context){val config=Configuration(base.resources.configuration);config.fontScale=1f;super.attachBaseContext(base.createConfigurationContext(config))}
 override fun onCreate(savedInstanceState: Bundle?){super.onCreate(savedInstanceState);enableEdgeToEdge()
  val language=intent.getStringExtra("language") ?: "es"
  val config=Configuration(resources.configuration).apply {setLocales(LocaleList(Locale.forLanguageTag(language)));fontScale=1f}
  val localized=createConfigurationContext(config)
  val screen=intent.getStringExtra("screen") ?: "vets"
  val role=if(screen in listOf("vets","invite"))Role.GANADERO else Role.VETERINARIO
  setContent {CompositionLocalProvider(androidx.compose.ui.platform.LocalContext provides localized,androidx.compose.ui.platform.LocalResources provides localized.resources,androidx.compose.ui.platform.LocalConfiguration provides config){AniTecTheme{
   val vm=remember {LinkingViewModel(DebugLinkingRepository(intent.getStringExtra("scenario") ?: "content"),role)}
   DisposableEffect(vm){vm.refresh();onDispose{vm.close()}}
   Scaffold(bottomBar={if(role==Role.GANADERO) RancherBottomBar(RancherTab.VETS,mapOf(RancherTab.HOME to stringResource(R.string.lk_home),RancherTab.CATTLE to stringResource(R.string.ls_tab_cattle),RancherTab.VETS to stringResource(R.string.lk_vets),RancherTab.PROFILE to stringResource(R.string.lk_profile))){} else VeterinarianBottomBar(VeterinarianTab.RANCHERS,{},mapOf(VeterinarianTab.HOME to stringResource(R.string.lk_home),VeterinarianTab.AGENDA to stringResource(R.string.lk_agenda),VeterinarianTab.RANCHERS to stringResource(R.string.lk_farmers),VeterinarianTab.PROFILE to stringResource(R.string.lk_profile)))}){padding->Box(Modifier.padding(padding).consumeWindowInsets(padding)){
   when(screen){"invite"->InviteVetScreen(vm){};"invitations"->VeterinarianInvitationsScreen(vm){};"farmers"->VeterinarianFarmersScreen(vm,{},{})
    "detail"->LinkedFarmerDetailScreen(vm,DebugLinkingRepository.FARMER){};else->RancherLinksScreen(vm){}}
   }}
  }}}
 }
}
