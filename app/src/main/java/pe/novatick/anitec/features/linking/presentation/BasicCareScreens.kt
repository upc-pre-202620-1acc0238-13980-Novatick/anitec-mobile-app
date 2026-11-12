package pe.novatick.anitec.features.linking.presentation
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable internal fun LatestCareSection(animal:AnimalDetail,open:(String)->Unit){
 LsText(stringResource(R.string.care_latest),bold=true)
 val items=animal.recentCare
 when{items==null->LsNotice(stringResource(R.string.care_unavailable));items.isEmpty()->LsNotice(stringResource(R.string.care_empty));else->{val item=items.first()
  Surface(onClick={open(item.id)},Modifier.fillMaxWidth().testTag("care_latest"),shape=RoundedCornerShape(15.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
   Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
    Icon(painterResource(R.drawable.ls_document_official),null,Modifier.size(20.dp),tint=Color.Unspecified)
    Column(Modifier.weight(1f)){LsText(item.title,size=12,bold=true);LsText(careDateLabel(item.date,LocalConfiguration.current.locales[0])+" · "+item.vetName,size=10,color=NeutralSlate)}
    Icon(painterResource(R.drawable.visit_chevron_official),null,Modifier.size(17.dp),tint=Color.Unspecified)
   }
  }
 }}
}
@Composable fun BasicCareForm(vm:BasicCareViewModel,farmer:String,animal:String,back:()->Unit,saved:()->Unit){
 val s by vm.state.collectAsState();LaunchedEffect(farmer,animal){vm.begin(farmer,animal)}
 LaunchedEffect(s.saved){if(s.saved!=null){vm.consumeSaved();saved()}}
 BackHandler(s.busy){}
 val locked=s.busy || s.uncertain
 var picker by remember{mutableStateOf(false)}
 if(picker)VisitDatePicker(System.currentTimeMillis(),{picker=false}){vm.date(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date(it)));picker=false}
 AnimalPage(stringResource(R.string.lk_record_care),back,busy=s.busy,subtitle=stringResource(R.string.care_form_sub),pinnedHeader=true){
  LsText(stringResource(R.string.care_date),size=12)
  Surface(onClick={picker=true},enabled=!locked,modifier=Modifier.fillMaxWidth().testTag("care_date"),shape=RoundedCornerShape(13.dp),color=LivestockVisuals.Pale){
   Row(Modifier.heightIn(min=48.dp).padding(horizontal=13.dp),verticalAlignment=Alignment.CenterVertically){
    LsText(if(validCareDate(s.date))careDateLabel(s.date,LocalConfiguration.current.locales[0])else "",Modifier.weight(1f),size=12)
    Icon(painterResource(R.drawable.link_calendar_official),null,Modifier.size(20.dp),tint=NeutralSlate)
   }
  }
  if(s.attempted && (!validCareDate(s.date) || s.date>SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())))LsText(stringResource(R.string.care_invalid),size=11,color=MaterialTheme.colorScheme.error)
  AnimalField(s.title,vm::title,stringResource(R.string.care_reason),"","care_title",if(s.attempted && (s.title.isBlank() || s.title.length>150))stringResource(R.string.care_invalid)else null,!locked)
  LsText(stringResource(R.string.care_description),size=12)
  BasicTextField(s.description,vm::description,enabled=!locked,textStyle=TextStyle(fontFamily=Poppins,fontSize=12.sp,color=LivestockVisuals.Ink),cursorBrush=SolidColor(PrimaryGreen),
   modifier=Modifier.fillMaxWidth().heightIn(min=86.dp).background(LivestockVisuals.Pale,RoundedCornerShape(13.dp)).padding(13.dp).testTag("care_description"))
  if(s.attempted && s.description.isBlank())LsText(stringResource(R.string.care_invalid),size=11,color=MaterialTheme.colorScheme.error)
  s.error?.let{LsNotice(animalFailureText(it),true)}
  if(s.uncertain)LsNotice(stringResource(R.string.care_uncertain),true)
  if(s.busy)LsLoading(stringResource(R.string.lk_loading))
  LsAction(stringResource(R.string.care_save),vm::save,Modifier.fillMaxWidth().testTag("care_save"),primary=true,enabled=!locked)
 }
}
@Composable fun BasicCareDetail(vm:BasicCareViewModel,animal:String,id:String,animalInfo:AnimalDetail?=null,back:()->Unit){
 val s by vm.state.collectAsState();LaunchedEffect(animal,id){vm.read(id,animal)}
 AnimalPage(stringResource(R.string.care_detail),back){
  if(s.reading)LsLoading(stringResource(R.string.lk_loading))
  s.readError?.let{LsNotice(animalFailureText(it),true);LsAction(stringResource(R.string.lk_reload),{vm.read(id,animal)})}
  s.detail?.let{value->
   animalInfo?.takeIf{it.id==value.animalId}?.let{info->Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
    Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)){
     AnimalSpeciesImage(info.species)
     Column{LsText(info.name?.takeIf(String::isNotBlank) ?: info.code,bold=true,color=PrimaryGreen);LsText(info.code+" · "+info.species,size=11,color=NeutralSlate)}
    }
   }}
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
    Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
     LsText(stringResource(R.string.care_related),size=10,color=NeutralSlate);LsText(value.title,bold=true)
     LsText(stringResource(R.string.lk_veterinarian_role),size=10,color=NeutralSlate);LsText(value.vetName,size=12,bold=true)
     LsText(stringResource(R.string.care_date_read),size=10,color=NeutralSlate);LsText(careDateLabel(value.date,LocalConfiguration.current.locales[0]),size=12,bold=true)
    }
   }
   LsText(stringResource(R.string.care_summary),bold=true)
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),color=LivestockVisuals.Pale){LsText(value.description,Modifier.padding(14.dp),size=12,color=LivestockVisuals.Secondary)}
  }
 }
}
