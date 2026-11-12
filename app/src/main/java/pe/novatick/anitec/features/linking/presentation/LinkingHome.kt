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
import pe.novatick.anitec.features.linking.domain.VisitDay
import kotlinx.coroutines.delay
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.components.RancherHeaderShape
import pe.novatick.anitec.ui.theme.*

/** Figma 104:3913; today's visits are read-only, full agenda and clinical actions remain unavailable. */
@Composable fun VeterinaryHome(vm: LinkingViewModel,profile: Profile,farmers: ()->Unit,invitations: ()->Unit,account: ()->Unit) {
 val s by vm.state.collectAsState()
 LaunchedEffect(vm){while(true){vm.readToday();val now=System.currentTimeMillis();delay((VisitDay.current(now).end-now).coerceAtLeast(1000))}}
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
   Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
    HomeMetric(stringResource(R.string.lk_visits_today),s.today.value?.size?.toString() ?: "—",R.drawable.link_visit_metric_official,"home_visit_count",Modifier.weight(1f))
    HomeMetric(stringResource(R.string.lk_linked_farmers),s.farmers.value?.size?.toString() ?: "—",R.drawable.link_home_farmers_official,"home_link_count",Modifier.weight(1f),farmers)
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
    VeterinaryShortcut(stringResource(R.string.lk_schedule),R.drawable.link_schedule_official,Modifier.weight(1f).testTag("home_schedule_unavailable"),available=false)
    VeterinaryShortcut(stringResource(R.string.lk_record_care),R.drawable.link_attention_official,Modifier.weight(1f).testTag("home_care_unavailable"),available=false)
   }
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){LsText(stringResource(R.string.lk_agenda_today),Modifier.weight(1f),bold=true);TextButton({},enabled=false){LsText(stringResource(R.string.lk_view_agenda),size=10,color=NeutralSlate)}}
   if(s.today.loading)LsLoading(stringResource(R.string.lk_loading))
   s.today.error?.let {LinkError(it);LsAction(stringResource(R.string.lk_reload),{vm.readToday()})}
   s.today.value?.let {items->if(items.isEmpty())VeterinaryAgendaPlaceholder(today=true) else Surface(shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
    Column {items.forEachIndexed {index,item->
     if(index>0)HorizontalDivider(color=LivestockVisuals.Border)
     Row(Modifier.fillMaxWidth().padding(12.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){
      val locale=androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
      LsText(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT,locale).format(java.util.Date(item.time)),size=10,bold=true,color=PrimaryGreen)
      Column(Modifier.weight(1f)){
       LsText(item.reason?.takeIf(String::isNotBlank) ?: stringResource(R.string.lk_visit),size=11,bold=true)
       LsText(listOfNotNull(item.animal,item.farmer).joinToString(" · "),size=9,color=NeutralSlate)
       LsText(stringResource(when(item.status){"COMPLETADA"->R.string.lk_visit_completed;"CANCELADA"->R.string.lk_visit_cancelled;else->R.string.lk_visit_scheduled}),size=9,color=LivestockVisuals.Secondary)
      }
     }
    }}
   }}
  }
 }
}
@Composable private fun HomeMetric(label:String,count:String,icon:Int,tag:String,modifier:Modifier,onClick:(()->Unit)?=null){
 val content:@Composable ()->Unit={Row(Modifier.heightIn(min=97.dp).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){
  Box(Modifier.size(40.dp).background(LivestockVisuals.Mint,RoundedCornerShape(8.dp)),contentAlignment=Alignment.Center){Icon(painterResource(icon),null,if(icon==R.drawable.link_visit_metric_official)Modifier.size(25.dp,26.dp) else Modifier.size(23.dp),tint=Color.Unspecified)}
  Column(Modifier.weight(1f)){LsText(count,Modifier.testTag(tag),size=24,bold=true);LsText(label,size=9,color=LivestockVisuals.Secondary)}
  Icon(painterResource(R.drawable.link_home_chevron_official),null,Modifier.size(8.25.dp,14.5.dp),tint=Color.Unspecified)
 }}
 if(onClick==null)Surface(modifier,shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border),shadowElevation=5.dp,content=content)
 else Surface(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border),shadowElevation=5.dp,content=content)
}
@Composable internal fun VeterinaryShortcut(label: String,icon: Int,modifier: Modifier,available:Boolean=true,onClick: ()->Unit={}) {
 Surface(onClick=onClick,enabled=available,shape=RoundedCornerShape(15.dp),color=Color(0xFFF4FCF7),border=BorderStroke(1.dp,LivestockVisuals.Mint),modifier=modifier) {
  Row(Modifier.heightIn(min=64.dp).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
   Box(Modifier.size(33.dp).background(PrimaryGreenAccent,RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center){Icon(painterResource(icon),null,Modifier.size(if(icon==R.drawable.link_home_farmers_official)23.dp else 20.dp),tint=Color.White)}
   Column(Modifier.weight(1f)){LsText(label,size=10,bold=true);if(!available)LsText(stringResource(R.string.lk_action_soon),size=9,color=LivestockVisuals.Secondary)}
  }
 }
}
@Composable internal fun VeterinaryAgendaPlaceholder(today:Boolean=false) {
 Surface(shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border),modifier=Modifier.fillMaxWidth()) {
  Column(Modifier.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
   Icon(painterResource(R.drawable.link_calendar_official),null,Modifier.size(20.dp),tint=NeutralSlate)
   LsText(stringResource(if(today)R.string.lk_today_empty else R.string.lk_upcoming_empty),size=12,color=LivestockVisuals.Secondary)
   LsText(stringResource(R.string.lk_agenda_soon),size=10,color=NeutralSlate)
  }
 }
}
