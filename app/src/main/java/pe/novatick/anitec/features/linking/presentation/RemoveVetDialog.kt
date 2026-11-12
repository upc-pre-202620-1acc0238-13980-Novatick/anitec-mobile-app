package pe.novatick.anitec.features.linking.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.livestock.presentation.LsText
import pe.novatick.anitec.ui.theme.PrimaryGreen

/** Same full-screen 44% scrim pattern as the approved animal deactivation dialog. */
@Composable internal fun RemoveVetDialog(name: String,cancel: ()->Unit,confirm: ()->Unit) {
 Dialog(cancel,DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
  val view=LocalView.current
  val window=generateSequence(view.parent){it.parent}.filterIsInstance<DialogWindowProvider>().firstOrNull()?.window
  SideEffect{window?.setDimAmount(0f);window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)}
  Box(Modifier.fillMaxSize()) {
   Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.44f)).testTag("remove_vet_scrim").pointerInput(Unit){detectTapGestures{cancel()}})
   Box(Modifier.fillMaxSize().systemBarsPadding().padding(22.dp),contentAlignment=Alignment.Center) {
    Surface(Modifier.widthIn(max=332.dp).fillMaxWidth().testTag("remove_vet_dialog"),shape=RoundedCornerShape(20.dp),color=Color.White) {
     Column(Modifier.padding(22.dp).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
      Text(stringResource(R.string.lk_remove_title),fontFamily=pe.novatick.anitec.ui.theme.Poppins,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold,style=MaterialTheme.typography.titleSmall,color=PrimaryGreen,textAlign=TextAlign.Center)
      LsText(name,size=12,bold=true)
      Text(stringResource(R.string.lk_remove_info),fontFamily=pe.novatick.anitec.ui.theme.Poppins,style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center)
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
       Button(cancel,Modifier.weight(1f).heightIn(min=48.dp),shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(6.dp),colors=ButtonDefaults.buttonColors(containerColor=pe.novatick.anitec.ui.theme.PrimaryGreenAccent)) {LsText(stringResource(R.string.lk_cancel),size=11,bold=true,color=Color.White)}
       OutlinedButton(confirm,Modifier.weight(1f).heightIn(min=48.dp).testTag("remove_vet_confirm"),shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,Color(0xFFDF6262)),contentPadding=PaddingValues(6.dp),colors=ButtonDefaults.outlinedButtonColors(containerColor=Color.White)) {LsText(stringResource(R.string.lk_remove),size=11,bold=true,color=Color(0xFFDF6262))}
      }
     }
    }
   }
  }
 }
}
