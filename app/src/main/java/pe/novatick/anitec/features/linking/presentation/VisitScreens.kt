package pe.novatick.anitec.features.linking.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.linking.domain.*
import pe.novatick.anitec.features.livestock.presentation.*
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable internal fun visitDate(value:Long,pattern:String):String=SimpleDateFormat(pattern,LocalConfiguration.current.locales[0]).format(Date(value))
@Composable internal fun visitTime(value:Long)=DateFormat.getTimeInstance(DateFormat.SHORT,LocalConfiguration.current.locales[0]).format(Date(value))
private fun civil(value:Long)=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date(value))
private fun shiftDay(day:VisitDay,amount:Int):VisitDay=Calendar.getInstance().apply{timeInMillis=day.start;add(Calendar.DAY_OF_MONTH,amount)}.let{VisitDay.current(it.timeInMillis)}
@Composable internal fun VisitReadError(error:AuthFailure){
 LsNotice(stringResource(when{error.status==403->R.string.lk_denied;error.status==404->R.string.vis_missing;error.status==400 || error.code=="INVALID_DATE"->R.string.vis_invalid;error.problem in listOf(AuthProblem.RELOGIN,AuthProblem.UNAUTHORIZED)->R.string.lk_session;else->R.string.vis_read_error}),true)
}
@Composable internal fun VisitRows(items:List<HomeVisit>,select:(HomeVisit)->Unit){
 Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)){
  Column{items.forEachIndexed{index,item->
   if(index>0)HorizontalDivider(color=LivestockVisuals.Border)
   Surface(onClick={select(item)},color=Color.White,modifier=Modifier.fillMaxWidth().testTag("visit_"+item.id)){
    Row(Modifier.padding(horizontal=10.dp,vertical=13.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)){
     LsText(visitTime(item.time),size=10,bold=true,color=PrimaryGreen)
     val status=stringResource(when(item.status){"COMPLETADA"->R.string.lk_visit_completed;"CANCELADA"->R.string.lk_visit_cancelled;else->R.string.lk_visit_scheduled})
     Box(Modifier.size(15.dp).background(LivestockVisuals.Mint,CircleShape).padding(4.dp).background(when(item.status){"CANCELADA"->Color(0xFFDF6262);"COMPLETADA"->PrimaryGreen;else->PrimaryGreenAccent},CircleShape).semantics{contentDescription=status})
     Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
      LsText(item.reason?.takeIf(String::isNotBlank) ?: stringResource(R.string.lk_visit),size=11,bold=true)
      LsText(listOfNotNull(item.animal,item.farmer).joinToString(" · "),size=9,color=NeutralSlate)
     }
     Icon(painterResource(R.drawable.visit_chevron_official),null,Modifier.size(17.dp),tint=Color.Unspecified)
    }
   }
  }}
 }
}
@Composable fun VisitsAgendaScreen(vm:VisitsViewModel,schedule:()->Unit,select:(HomeVisit)->Unit){
 val s by vm.state.collectAsState();var picker by remember{mutableStateOf(false)}
 LaunchedEffect(vm){vm.load()}
 if(picker)VisitDatePicker(s.day.start,{picker=false}){vm.load(VisitDay.current(it));picker=false}
 Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())){
  Box(Modifier.fillMaxWidth().height(30.dp).clip(CattleHeaderShape).background(PrimaryGreenAccent))
  Column(Modifier.padding(horizontal=28.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
   Column(verticalArrangement=Arrangement.spacedBy(3.dp)){
    LsText(stringResource(R.string.vis_agenda),size=24,bold=true,color=PrimaryGreen)
    LsText(stringResource(R.string.vis_agenda_sub),size=13,color=NeutralSlate)
   }
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
    IconButton({vm.load(shiftDay(s.day,-5))},Modifier.size(48.dp).testTag("vis_previous_days")){Icon(painterResource(R.drawable.ic_arrow_left),stringResource(R.string.vis_previous),Modifier.size(18.dp),tint=PrimaryGreen)}
    TextButton({picker=true},Modifier.weight(1f).testTag("vis_calendar")){LsText(visitDate(s.day.start,"MMMM yyyy"),size=12,bold=true,color=PrimaryGreen)}
    IconButton({vm.load(shiftDay(s.day,5))},Modifier.size(48.dp).testTag("vis_next_days")){Icon(painterResource(R.drawable.link_chevron_official),stringResource(R.string.vis_next),Modifier.size(17.dp),tint=PrimaryGreen)}
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
    (-2..2).forEach{offset->val day=shiftDay(s.day,offset);val selected=offset==0
     Surface(onClick={vm.load(day)},shape=RoundedCornerShape(16.dp),color=if(selected)PrimaryGreenMedium else LivestockVisuals.Pale,modifier=Modifier.weight(1f).testTag("vis_day_"+civil(day.start))){
      Column(Modifier.heightIn(min=57.dp).padding(vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally){
       LsText(visitDate(day.start,"EEE").uppercase(LocalConfiguration.current.locales[0]),size=9,color=if(selected)Color.White else NeutralSlate)
       LsText(visitDate(day.start,"d"),size=20,bold=true,color=if(selected)Color.White else NeutralSlate)
      }
     }
    }
   }
   LsAction(stringResource(R.string.lk_schedule),schedule,Modifier.fillMaxWidth().testTag("vis_schedule"),primary=true,icon=R.drawable.ls_plus_official)
   LsText(visitDate(s.day.start,"EEEE, d MMMM"),bold=true)
   if(s.agenda.loading)LsLoading(stringResource(R.string.lk_loading))
   s.agenda.error?.let{VisitReadError(it);LsAction(stringResource(R.string.lk_reload),{vm.load()})}
   s.agenda.value?.let{if(it.isEmpty())VeterinaryAgendaPlaceholder(today=true,available=true) else VisitRows(it,select)}
  }
 }
}
@Composable fun VisitDetailScreen(vm:VisitsViewModel,id:String,at:Long,vetName:String,back:()->Unit){
 val s by vm.state.collectAsState();LaunchedEffect(id,at){vm.loadDetail(id,VisitDay.current(at))}
 AnimalPage(stringResource(R.string.vis_detail),back,subtitle=s.detail?.reason,titleSize=20){
  if(s.detailLoading)LsLoading(stringResource(R.string.lk_loading))
  s.detailError?.let{VisitReadError(it);LsAction(stringResource(R.string.lk_reload),{vm.loadDetail(id,VisitDay.current(at))})}
  s.detail?.let{item->
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=PrimaryGreenAccent){
    Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
     Box(Modifier.size(53.dp).background(Color.White.copy(alpha=.16f),RoundedCornerShape(15.dp)),contentAlignment=Alignment.Center){Icon(painterResource(R.drawable.visit_banner_calendar_official),null,Modifier.size(32.dp),tint=Color.Unspecified)}
     Column{LsText(visitDate(item.time,"EEEE, d MMMM").uppercase(LocalConfiguration.current.locales[0]),size=8,bold=true,color=Color.White);LsText(visitTime(item.time),size=28,bold=true,color=Color.White);LsText(stringResource(when(item.status){"COMPLETADA"->R.string.lk_visit_completed;"CANCELADA"->R.string.lk_visit_cancelled;else->R.string.lk_visit_scheduled}),size=9,color=Color.White)}
    }
   }
   LsText(stringResource(R.string.vis_information),Modifier.padding(top=8.dp),bold=true)
   Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White){Column{
    VisitInfo(R.drawable.visit_animal_official,22,16,stringResource(R.string.vis_animal),item.animal ?: item.animalId.orEmpty())
    VisitInfo(R.drawable.visit_person_official,25,25,stringResource(R.string.vis_farmer),item.farmer)
    VisitInfo(R.drawable.visit_vet_official,20,20,stringResource(R.string.lk_veterinarian_role),vetName)
    VisitInfo(R.drawable.visit_reason_official,20,20,stringResource(R.string.vis_reason),item.reason?.takeIf(String::isNotBlank) ?: stringResource(R.string.lk_visit),last=true)
   }}
   Button({},Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=false,shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(disabledContainerColor=PrimaryGreenAccent,disabledContentColor=Color.White)){
    Icon(painterResource(R.drawable.link_attention_official),null,Modifier.size(20.dp),tint=Color.White);Spacer(Modifier.width(8.dp));LsText(stringResource(R.string.lk_record_care),size=12,bold=true,color=Color.White)
   }
   LsText(stringResource(R.string.lk_action_soon),size=10,color=NeutralSlate)
  }
 }
}
@Composable private fun VisitInfo(icon:Int,width:Int,height:Int,label:String,value:String,last:Boolean=false){
 Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)){
  Box(Modifier.width(25.dp),contentAlignment=Alignment.Center){Icon(painterResource(icon),null,Modifier.size(width.dp,height.dp),tint=Color.Unspecified)}
  Column(Modifier.weight(1f)){LsText(label,size=10,color=NeutralSlate);LsText(value,size=11,bold=true,color=PrimaryGreen)}
 };if(!last)HorizontalDivider(color=LivestockVisuals.Border)
}
@Composable fun VisitScheduleScreen(vm:VisitsViewModel,back:()->Unit,confirmed:()->Unit){
 val s by vm.state.collectAsState();val locked=s.form.busy || s.form.uncertain
 val context=LocalContext.current;val saved=stringResource(R.string.vis_saved)
 BackHandler(locked){}
 LaunchedEffect(s.form.confirmed){if(s.form.confirmed!=null && vm.dismiss()){android.widget.Toast.makeText(context,saved,android.widget.Toast.LENGTH_SHORT).show();confirmed()}}
 AnimalPage(stringResource(R.string.lk_schedule),{if(vm.dismiss())back()},busy=locked,subtitle=stringResource(R.string.vis_access_only),pinnedHeader=true,titleSize=20){
  VisitFormFields(vm,s.form,false)
  Row(Modifier.fillMaxWidth().background(Color(0xFFF4FCF7),RoundedCornerShape(12.dp)).padding(horizontal=14.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Icon(painterResource(R.drawable.link_calendar_official),null,Modifier.size(20.dp),tint=PrimaryGreen);LsText(stringResource(R.string.vis_private_agenda),Modifier.weight(1f),size=9,color=LivestockVisuals.Secondary)
  }
  VisitFormFeedback(vm,s)
  LsAction(stringResource(R.string.vis_save),vm::save,Modifier.fillMaxWidth().testTag("vis_save"),primary=true,enabled=!locked && s.form.confirmed==null)
 }
}
@Composable fun VisitScheduleModal(vm:VisitsViewModel,cancel:()->Unit,confirmed:()->Unit){
 val s by vm.state.collectAsState();val locked=s.form.busy || s.form.uncertain
 val context=LocalContext.current;val saved=stringResource(R.string.vis_saved)
 LaunchedEffect(s.form.confirmed){if(s.form.confirmed!=null && vm.dismiss()){android.widget.Toast.makeText(context,saved,android.widget.Toast.LENGTH_SHORT).show();confirmed()}}
 VisitOverlay(stringResource(R.string.lk_schedule),{if(vm.dismiss())cancel()},locked,actions={
  Button({if(vm.dismiss())cancel()},Modifier.weight(1f).heightIn(min=48.dp).testTag("vis_cancel"),enabled=!locked,shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFF0F0))){LsText(stringResource(R.string.lk_cancel),size=12,bold=true,color=Color(0xFFDF6262))}
  LsAction(stringResource(R.string.vis_save_short),vm::save,Modifier.weight(1f).testTag("vis_save"),primary=true,enabled=!locked && s.form.confirmed==null)
 }){VisitFormFields(vm,s.form,true);VisitFormFeedback(vm,s)}
}
@Composable private fun VisitFormFields(vm:VisitsViewModel,f:VisitFormState,modal:Boolean){
 val enabled=!f.busy && !f.uncertain && (!modal || f.animals.value?.any{it.id==f.animal}==true);var datePicker by remember{mutableStateOf(false)};var timePicker by remember{mutableStateOf(false)}
 if(datePicker)VisitDatePicker(visitLocalMillis(f.date,"12:00") ?: System.currentTimeMillis(),{datePicker=false}){vm.date(civil(it));datePicker=false}
 if(timePicker)VisitTimePicker(f.time,{timePicker=false}){vm.time(it);timePicker=false}
 if(!modal){
  if(f.farmers.loading)LsLoading(stringResource(R.string.lk_loading))
  f.farmers.error?.let{VisitReadError(it);LsAction(stringResource(R.string.lk_reload),{vm.begin()})}
  AnimalSelect(stringResource(R.string.vis_farmer)+" *",f.farmers.value?.firstOrNull{it.id==f.owner}?.name ?: stringResource(R.string.vis_select),f.farmers.value.orEmpty().map{it.id to it.name},enabled,"vis_farmer",valueColor=if(f.owner==null)NeutralSlate else LivestockVisuals.Ink,onSelect={vm.chooseOwner(it)})
  if(f.animals.loading)LsLoading(stringResource(R.string.lk_loading))
  f.animals.error?.let{VisitReadError(it);f.owner?.let{owner->LsAction(stringResource(R.string.lk_reload),{vm.chooseOwner(owner)})}}
  AnimalSelect(stringResource(R.string.vis_animal)+" *",f.animals.value?.firstOrNull{it.id==f.animal}?.let{it.name ?: it.code} ?: stringResource(R.string.vis_select),f.animals.value.orEmpty().map{it.id to (it.name ?: it.code)},enabled && f.owner!=null && !f.animals.loading,"vis_animal",valueColor=if(f.animal==null)NeutralSlate else LivestockVisuals.Ink,onSelect=vm::chooseAnimal)
 }else{
  if(f.farmers.loading || f.animals.loading)LsLoading(stringResource(R.string.lk_loading))
  f.farmers.error?.let{VisitReadError(it)};f.animals.error?.let{VisitReadError(it)}
  if(f.farmers.error!=null || f.animals.error!=null)LsAction(stringResource(R.string.lk_reload),{vm.begin(f.owner,f.animal)})
  if(f.farmers.value!=null && f.farmers.value.none{it.id==f.owner})LsNotice(stringResource(R.string.lk_denied),true)
  if(f.animals.value!=null && f.animals.value.none{it.id==f.animal})LsNotice(stringResource(R.string.animal_missing),true)
 }
 val reasonLabel=stringResource(R.string.vis_reason)
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(6.dp)){
 LsText(reasonLabel+" *",size=12)
 BasicTextField(f.reason,vm::reason,enabled=enabled,textStyle=TextStyle(fontFamily=Poppins,fontSize=12.sp,color=LivestockVisuals.Ink),modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).background(LivestockVisuals.Pale,RoundedCornerShape(14.dp)).padding(14.dp).testTag("vis_reason").semantics{contentDescription=reasonLabel},decorationBox={inner->Box{if(f.reason.isEmpty())LsText(stringResource(R.string.vis_reason_hint),size=12,color=NeutralSlate);inner()}})
 }
 val dateContent:@Composable (Modifier)->Unit={m->VisitPickerField(stringResource(R.string.vis_date)+" *",visitLocalMillis(f.date,"12:00")?.let{visitDate(it,"dd/MM/yyyy")} ?: stringResource(R.string.vis_date_hint),R.drawable.link_calendar_official,20,m.testTag("vis_date"),enabled){datePicker=true}}
 val timeContent:@Composable (Modifier)->Unit={m->VisitPickerField(stringResource(R.string.vis_time)+" *",f.time.ifBlank{"--:--"},R.drawable.visit_clock_official,16,m.testTag("vis_time"),enabled){timePicker=true}}
 if(modal){dateContent(Modifier.fillMaxWidth());timeContent(Modifier.fillMaxWidth())}else Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){dateContent(Modifier.weight(1f));timeContent(Modifier.weight(1f))}
 if(f.attempted && !vm.canSave())LsNotice(stringResource(R.string.vis_invalid),true)
}
@Composable private fun VisitFormFeedback(vm:VisitsViewModel,s:VisitsState){
 val f=s.form;if(f.busy)LsLoading(stringResource(R.string.vis_saving))
 f.error?.let{VisitReadError(it)}
 if(f.uncertain){
  LsNotice(stringResource(R.string.vis_uncertain),true)
  LsAction(stringResource(R.string.vis_check),vm::checkUncertain,enabled=!f.busy)
  s.agenda.value?.forEach{LsText(visitTime(it.time)+" · "+(it.reason ?: "")+" · "+it.animal.orEmpty(),size=10)}
  LsAction(stringResource(R.string.vis_ack),vm::acknowledgeUncertain,enabled=f.checked && !f.busy)
 }
}
@Composable private fun VisitPickerField(label:String,value:String,icon:Int,size:Int,modifier:Modifier,enabled:Boolean,click:()->Unit){
 Column(modifier,verticalArrangement=Arrangement.spacedBy(6.dp)){LsText(label,size=12)
  Surface(onClick=click,enabled=enabled,shape=RoundedCornerShape(14.dp),color=LivestockVisuals.Pale,modifier=Modifier.fillMaxWidth().semantics{contentDescription=label}){
   Row(Modifier.heightIn(min=48.dp).padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){LsText(value,Modifier.weight(1f),size=12,color=NeutralSlate);Icon(painterResource(icon),null,Modifier.size(size.dp),tint=Color.Unspecified)}
  }
 }
}
@Composable private fun VisitOverlay(title:String,dismiss:()->Unit,locked:Boolean=false,actions:@Composable RowScope.()->Unit={},content:@Composable ColumnScope.()->Unit){
 Dialog({if(!locked)dismiss()},DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false,dismissOnBackPress=!locked,dismissOnClickOutside=!locked)){
  val view=LocalView.current;val window=generateSequence(view.parent){it.parent}.filterIsInstance<DialogWindowProvider>().firstOrNull()?.window
  SideEffect{window?.setDimAmount(0f);window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)}
  Box(Modifier.fillMaxSize()){
   Box(Modifier.matchParentSize().background(Color.Black.copy(alpha=.44f)).pointerInput(locked){detectTapGestures{if(!locked)dismiss()}})
   Box(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(22.dp),contentAlignment=Alignment.Center){
    Surface(Modifier.widthIn(max=332.dp).fillMaxWidth().heightIn(max=620.dp).testTag("visit_modal"),shape=RoundedCornerShape(20.dp),color=Color.White){
     Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
      Text(title,Modifier.fillMaxWidth(),fontFamily=Poppins,fontSize=20.sp,lineHeight=25.sp,fontWeight=FontWeight.Bold,color=PrimaryGreen,textAlign=TextAlign.Center)
      Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp),content=content)
      Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp),content=actions)
     }
    }
   }
  }
 }
}
@Composable private fun VisitDatePicker(initial:Long,dismiss:()->Unit,choose:(Long)->Unit){
 var month by remember{mutableStateOf(Calendar.getInstance().apply{timeInMillis=initial;set(Calendar.DAY_OF_MONTH,1);set(Calendar.HOUR_OF_DAY,12);set(Calendar.MINUTE,0)}.timeInMillis)}
 VisitOverlay(stringResource(R.string.vis_date),dismiss,actions={TextButton(dismiss){LsText(stringResource(R.string.lk_cancel),color=PrimaryGreen)}}){
  Row(verticalAlignment=Alignment.CenterVertically){IconButton({month=Calendar.getInstance().apply{timeInMillis=month;add(Calendar.MONTH,-1)}.timeInMillis},Modifier.testTag("vis_month_previous")){LsText("‹",size=22,color=PrimaryGreen)};LsText(visitDate(month,"MMMM yyyy"),Modifier.weight(1f),size=12,bold=true);IconButton({month=Calendar.getInstance().apply{timeInMillis=month;add(Calendar.MONTH,1)}.timeInMillis},Modifier.testTag("vis_month_next")){LsText("›",size=22,color=PrimaryGreen)}}
  val calendar=Calendar.getInstance().apply{timeInMillis=month};val first=(calendar.get(Calendar.DAY_OF_WEEK)+5)%7;val days=calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
  val locale=LocalConfiguration.current.locales[0]
  Row(Modifier.fillMaxWidth()){(0..6).forEach{offset->val c=Calendar.getInstance().apply{set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);add(Calendar.DAY_OF_MONTH,offset)};Text(SimpleDateFormat("EEEEE",locale).format(c.time),Modifier.weight(1f),fontFamily=Poppins,fontSize=9.sp,textAlign=TextAlign.Center,color=NeutralSlate)}}
  (0 until (first+days+6)/7).forEach{week->Row(Modifier.fillMaxWidth()){
   (0..6).forEach{weekday->val day=week*7+weekday-first+1
    if(day !in 1..days)Spacer(Modifier.weight(1f).height(40.dp)) else {
     val stamp=Calendar.getInstance().apply{timeInMillis=month;set(Calendar.DAY_OF_MONTH,day)}.timeInMillis
     Surface(onClick={choose(stamp)},shape=CircleShape,color=if(civil(stamp)==civil(initial))LivestockVisuals.Mint else Color.White,modifier=Modifier.weight(1f).heightIn(min=40.dp).testTag("vis_pick_"+civil(stamp))){Box(contentAlignment=Alignment.Center){LsText(day.toString(),size=12,color=PrimaryGreen)}}
    }
   }
  }}
 }
}
@Composable private fun VisitTimePicker(value:String,dismiss:()->Unit,choose:(String)->Unit){
 var hour by remember{mutableStateOf(value.take(2).ifBlank{"10"})};var minute by remember{mutableStateOf(value.takeLast(2).ifBlank{"00"})}
 val valid=(hour.toIntOrNull() ?: -1) in 0..23 && (minute.toIntOrNull() ?: -1) in 0..59
 VisitOverlay(stringResource(R.string.vis_time),dismiss,actions={TextButton(dismiss){LsText(stringResource(R.string.lk_cancel),color=PrimaryGreen)};LsAction(stringResource(R.string.vis_choose),{choose(hour.padStart(2,'0')+":"+minute.padStart(2,'0'))},Modifier.weight(1f).testTag("vis_time_choose"),primary=true,enabled=valid)}){
  Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
   OutlinedTextField(hour,{hour=it.filter(Char::isDigit).take(2)},Modifier.weight(1f).testTag("vis_hour"),label={LsText(stringResource(R.string.vis_hour),size=12)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),textStyle=TextStyle(fontFamily=Poppins,fontSize=20.sp),singleLine=true)
   OutlinedTextField(minute,{minute=it.filter(Char::isDigit).take(2)},Modifier.weight(1f).testTag("vis_minute"),label={LsText(stringResource(R.string.vis_minute),size=12)},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),textStyle=TextStyle(fontFamily=Poppins,fontSize=20.sp),singleLine=true)
  }
 }
}
