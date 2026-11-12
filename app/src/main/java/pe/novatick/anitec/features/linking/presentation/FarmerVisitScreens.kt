package pe.novatick.anitec.features.linking.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.novatick.anitec.R
import pe.novatick.anitec.features.linking.domain.FarmerVisit
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.theme.*

@Composable private fun RefreshFarmerVisits(vm:FarmerVisitsViewModel){
 val owner=LocalLifecycleOwner.current
 DisposableEffect(owner,vm){val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_RESUME)vm.load()};owner.lifecycle.addObserver(observer);onDispose{owner.lifecycle.removeObserver(observer)}}
}
@Composable internal fun FarmerVisitCards(items:List<FarmerVisit>,select:(FarmerVisit)->Unit){
 Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {items.forEach{item->val v=item.visit
  Surface(onClick={select(item)},Modifier.fillMaxWidth().heightIn(min=66.dp).testTag("farmer_visit_"+v.id),shape=RoundedCornerShape(15.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
   Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
    Surface(Modifier.width(40.dp).heightIn(min=44.dp),shape=RoundedCornerShape(11.dp),color=LivestockVisuals.Mint){Column(Modifier.padding(vertical=5.dp),horizontalAlignment=Alignment.CenterHorizontally){
     LsText(visitDate(v.time,"d"),size=16,bold=true,color=PrimaryGreen)
     LsText(visitDate(v.time,"MMM").uppercase(LocalConfiguration.current.locales[0]),size=7,bold=true,color=PrimaryGreen)
    }}
    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
     LsText(listOfNotNull(v.animal,v.reason?.takeIf(String::isNotBlank)).joinToString(" · "),size=11,bold=true)
     Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(4.dp)){
      Icon(painterResource(R.drawable.ic_clock),null,Modifier.size(14.dp),tint=Color.Unspecified)
      LsText(visitTime(v.time)+" · "+item.vetName,Modifier.weight(1f),size=9,color=NeutralSlate)
     }
    }
    Icon(painterResource(R.drawable.visit_chevron_official),null,Modifier.size(17.dp),tint=Color.Unspecified)
   }
  }
 }}
}
@Composable private fun FarmerVisitsRead(s:FarmerVisitsState,retry:()->Unit,content:@Composable (List<FarmerVisit>)->Unit){
 if(s.loading)LsLoading(stringResource(R.string.lk_loading))
 s.error?.let{VisitReadError(it);LsAction(stringResource(R.string.lk_reload),retry)}
 s.items?.let{if(it.isEmpty()){
  Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
   Icon(painterResource(R.drawable.ic_calendar),null,Modifier.size(28.dp),tint=NeutralSlate)
   LsText(stringResource(R.string.fv_empty),size=12,color=NeutralSlate)
  }
 }else content(it)}
}
@Composable fun FarmerUpcomingHome(vm:FarmerVisitsViewModel,all:()->Unit,select:(FarmerVisit)->Unit){
 RefreshFarmerVisits(vm);val s by vm.state.collectAsState()
 Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
   LsText(stringResource(R.string.ls_upcoming_visits),bold=true)
   TextButton(all){LsText(stringResource(R.string.ls_view_visits),size=11,bold=true,color=PrimaryGreen)}
  }
  FarmerVisitsRead(s,vm::load){FarmerVisitCards(it.take(3),select)}
 }
}
@Composable fun FarmerUpcomingScreen(vm:FarmerVisitsViewModel,back:()->Unit,select:(FarmerVisit)->Unit){
 RefreshFarmerVisits(vm);val s by vm.state.collectAsState()
 AnimalPage(stringResource(R.string.ls_upcoming_visits),back,subtitle=stringResource(R.string.fv_subtitle)){
  FarmerVisitsRead(s,vm::load){FarmerVisitCards(it,select)}
 }
}
@Composable fun FarmerVisitDetailScreen(vm:FarmerVisitsViewModel,id:String,back:()->Unit){
 RefreshFarmerVisits(vm);val s by vm.state.collectAsState();val item=s.items?.firstOrNull{it.visit.id==id}
 AnimalPage(stringResource(R.string.vis_detail),back,subtitle=item?.visit?.reason,titleSize=20){
  if(s.loading)LsLoading(stringResource(R.string.lk_loading))
  s.error?.let{VisitReadError(it);LsAction(stringResource(R.string.lk_reload),vm::load)}
  if(s.items!=null && item==null)LsNotice(stringResource(R.string.vis_missing),false)
  item?.let{
   VisitDetailInformation(it.visit,it.vetName,listOfNotNull(it.visit.animal,it.animalCode.takeUnless{code->code==it.visit.animal}).joinToString(" · "),
    stringResource(if(it.visit.type=="CONTROL")R.string.fv_control else R.string.lk_visit))
  }
 }
}
