package pe.novatick.anitec.features.linking.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.Profile
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.components.RancherHeaderShape
import pe.novatick.anitec.ui.theme.*

/** Figma 104:3913; only linking data is live. Agenda remains explicitly unavailable. */
@Composable fun VeterinaryHome(vm: LinkingViewModel,profile: Profile,farmers: ()->Unit,invitations: ()->Unit,account: ()->Unit) {
 val s by vm.state.collectAsState()
 Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
  Column(Modifier.fillMaxWidth().clip(RancherHeaderShape).background(PrimaryGreenAccent).padding(horizontal=28.dp,vertical=30.dp)) {
   Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
     LsText(stringResource(R.string.lk_my_day),size=11,bold=true,color=Color.White.copy(alpha=.84f))
     Text(stringResource(R.string.ls_greeting,profile.name),fontFamily=Poppins,fontSize=28.sp,lineHeight=32.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.Bold,color=Color.White)
     LsText(stringResource(R.string.lk_home_sub),size=12,color=Color.White)
    }
    Surface(onClick=account,shape=CircleShape,color=Color.White,modifier=Modifier.size(49.dp)) {
     Box(contentAlignment=Alignment.Center){LsText(livestockInitials(profile.name,profile.lastName),size=20,bold=true,color=PrimaryGreen)}
    }
   }
  }
  Column(Modifier.padding(horizontal=28.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
   Surface(onClick=farmers,shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border),shadowElevation=5.dp,modifier=Modifier.fillMaxWidth()) {
    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     Box(Modifier.size(40.dp).background(LivestockVisuals.Mint,RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center) {Icon(painterResource(R.drawable.link_home_farmers_official),null,Modifier.size(23.dp),tint=Color.Unspecified)}
     Column(Modifier.weight(1f)) {
      if(s.farmers.loading)LsLoading(stringResource(R.string.lk_loading))
      s.farmers.value?.let {LsText(it.size.toString(),Modifier.testTag("home_link_count"),size=24,bold=true)}
      LsText(stringResource(R.string.lk_linked_farmers),size=11,color=LivestockVisuals.Secondary)
     }
     Icon(painterResource(R.drawable.link_chevron_official),null,Modifier.size(17.dp),tint=Color.Unspecified)
    }
   }
   s.farmers.error?.let {LinkError(it);LsAction(stringResource(R.string.lk_reload),vm::refresh)}
   s.pending.value?.takeIf {it.isNotEmpty()}?.let {pending->
    Surface(onClick=invitations,shape=RoundedCornerShape(15.dp),color=PrimaryGreenMedium,modifier=Modifier.fillMaxWidth().testTag("home_pending")) {
     Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
      Icon(painterResource(R.drawable.link_mail_official),null,Modifier.size(20.dp),tint=Color.White)
      Column(Modifier.weight(1f)) {LsText(pluralStringResource(R.plurals.lk_pending_count,pending.size,pending.size),size=11,bold=true,color=Color.White);LsText(stringResource(R.string.lk_review_invites),size=9,color=Color.White)}
     }
    }
   }
   s.pending.error?.let {LinkError(it);LsAction(stringResource(R.string.lk_reload),vm::refresh)}
   LsText(stringResource(R.string.lk_shortcuts),bold=true)
   Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
    HomeShortcut(stringResource(R.string.lk_farmers),R.drawable.link_home_farmers_official,Modifier.weight(1f),farmers)
    HomeShortcut(stringResource(R.string.lk_invitations),R.drawable.link_mail_official,Modifier.weight(1f),invitations)
   }
   LsText(stringResource(R.string.lk_agenda),bold=true)
   VeterinaryAgendaPlaceholder()
  }
 }
}
@Composable private fun HomeShortcut(label: String,icon: Int,modifier: Modifier,onClick: ()->Unit) {
 Surface(onClick=onClick,shape=RoundedCornerShape(15.dp),color=Color(0xFFF4FCF7),border=BorderStroke(1.dp,LivestockVisuals.Mint),modifier=modifier) {
  Row(Modifier.heightIn(min=64.dp).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   Icon(painterResource(icon),null,Modifier.size(if(icon==R.drawable.link_home_farmers_official)23.dp else 20.dp),tint=PrimaryGreen)
   LsText(label,size=10,bold=true)
  }
 }
}
@Composable internal fun VeterinaryAgendaPlaceholder() {
 Surface(shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border),modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Icon(painterResource(R.drawable.link_calendar_official),null,Modifier.size(20.dp),tint=NeutralSlate)
   LsText(stringResource(R.string.lk_upcoming_empty),size=12,color=LivestockVisuals.Secondary)
   LsText(stringResource(R.string.lk_agenda_soon),size=10,color=NeutralSlate)
  }
 }
}
