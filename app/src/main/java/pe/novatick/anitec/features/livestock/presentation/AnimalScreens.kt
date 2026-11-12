package pe.novatick.anitec.features.livestock.presentation

import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.PrimaryGreen
import pe.novatick.anitec.ui.theme.Poppins

@Composable private fun AnimalNavigationButton(icon: Int,label: String,enabled: Boolean=true,onClick: ()->Unit) {
    IconButton(onClick,enabled=enabled,modifier=Modifier.size(48.dp)) {
        Box(Modifier.size(34.dp).background(LivestockVisuals.Mint,RoundedCornerShape(11.dp)),contentAlignment=Alignment.Center) {
            Icon(painterResource(icon),label,Modifier.size(20.dp),tint=Color.Unspecified)
        }
    }
}
@Composable internal fun AnimalPage(title: String,back: ()->Unit,busy: Boolean=false,subtitle: String?=null,
    centered: Boolean=false,actions: (@Composable ()->Unit)?=null,pinnedHeader: Boolean=false,content: @Composable ColumnScope.()->Unit) {
    BackHandler(busy) {}
    val heading: @Composable ()->Unit = {
            Row(Modifier.fillMaxWidth().heightIn(min=62.dp).testTag("animal_header"),verticalAlignment=Alignment.CenterVertically) {
                AnimalNavigationButton(R.drawable.ic_arrow_left,stringResource(R.string.animal_back),!busy,back)
                Column(Modifier.weight(1f),horizontalAlignment=if(centered) Alignment.CenterHorizontally else Alignment.Start) {
                    Text(title,Modifier.fillMaxWidth().testTag("animal_header_title"),fontFamily=Poppins,fontSize=21.sp,lineHeight=26.25.sp,
                        fontWeight=FontWeight.Bold,color=PrimaryGreen,textAlign=if(centered) TextAlign.Center else TextAlign.Start)
                    subtitle?.let {Text(it,Modifier.fillMaxWidth(),fontFamily=Poppins,fontSize=12.sp,lineHeight=18.sp,
                        color=LivestockVisuals.Secondary,textAlign=if(centered) TextAlign.Center else TextAlign.Start)}
                }
                if(actions!=null) actions() else if(centered) Spacer(Modifier.width(48.dp))
            }
    }
    Column(Modifier.fillMaxSize().background(Color.White).imePadding().then(if(pinnedHeader) Modifier else Modifier.verticalScroll(rememberScrollState()))) {
        Box(Modifier.fillMaxWidth().height(30.dp).clip(CattleHeaderShape).background(pe.novatick.anitec.ui.theme.PrimaryGreenAccent))
        if(pinnedHeader) {
            Column(Modifier.padding(horizontal=28.dp,vertical=8.dp)) {heading()}
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=28.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(13.dp),content=content)
        } else Column(Modifier.padding(horizontal=28.dp,vertical=8.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {heading();content()}
    }
}
@Composable internal fun AnimalSelect(label: String,value: String,choices: List<Pair<String,String>>,enabled: Boolean,tag: String,onSelect: (String)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        LsText(label,size=12)
        Box {
            Surface(onClick={expanded=true},enabled=enabled,modifier=Modifier.fillMaxWidth().testTag(tag).semantics {contentDescription=label},shape=RoundedCornerShape(13.dp),
                color=LivestockVisuals.Pale) {
                Row(Modifier.heightIn(min=48.dp).padding(horizontal=13.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    LsText(value,Modifier.weight(1f),size=12)
                    Icon(painterResource(R.drawable.animal_dropdown_official),null,Modifier.size(10.dp,6.dp),tint=Color.Unspecified)
                }
            }
            DropdownMenu(expanded,{expanded=false},shape=RoundedCornerShape(14.dp),containerColor=Color.White,
                border=BorderStroke(1.dp,LivestockVisuals.Border)) {
                choices.forEach { (id,text)->DropdownMenuItem(text={LsText(text,size=12)},onClick={expanded=false;onSelect(id)})}
            }
        }
    }
}
@Composable internal fun AnimalField(value: String,onChange: (String)->Unit,label: String,help: String,tag: String,errorText: String?,
    enabled: Boolean=true,trailing: (@Composable ()->Unit)?=null) {
    var focused by remember {mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        LsText(label,size=12)
        BasicTextField(value,onChange,singleLine=true,enabled=enabled,textStyle=TextStyle(fontFamily=Poppins,fontSize=12.sp,color=LivestockVisuals.Ink),
            cursorBrush=SolidColor(PrimaryGreen),modifier=Modifier.fillMaxWidth().testTag(tag).onFocusChanged {focused=it.isFocused}.semantics {
                contentDescription=label
                if(errorText!=null) error(errorText)
            },decorationBox={editor ->
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp).background(LivestockVisuals.Pale,RoundedCornerShape(13.dp))
                    .border(1.dp,if(errorText!=null) Color(0xFFAE4141) else if(focused) PrimaryGreen else Color.Transparent,RoundedCornerShape(13.dp))
                    .padding(start=13.dp,end=if(trailing==null) 13.dp else 0.dp),verticalAlignment=Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).padding(vertical=12.dp)) {editor()}
                    trailing?.invoke()
                }
            })
        if(errorText!=null || help.isNotBlank()) LsText(errorText ?: help,size=11,color=if(errorText!=null) Color(0xFFAE4141) else LivestockVisuals.Secondary)
    }
}
/** Native accessible picker complements the editable ISO field; civil values never pass through UTC conversion. */
@Composable internal fun AnimalBirthField(value: String,onChange: (String)->Unit,error: String?,enabled: Boolean) {
    val host=LocalView.current.context
    val configuration=LocalConfiguration.current
    val context=remember(host,configuration) {android.view.ContextThemeWrapper(host,R.style.Theme_AniTec).apply {applyOverrideConfiguration(configuration)}}
    val label=stringResource(R.string.animal_birth)
    var open by remember {mutableStateOf(false)}
    AnimalField(value,onChange,label,stringResource(R.string.animal_date),"animal_birth",error,enabled) {
        IconButton(onClick={open=true},enabled=enabled,modifier=Modifier.testTag("animal_birth_picker")) {
            Icon(painterResource(R.drawable.ic_calendar),stringResource(R.string.animal_choose_date),Modifier.size(20.dp),tint=PrimaryGreen)
        }
    }
    if(open) DisposableEffect(context) {
        val calendar=Calendar.getInstance()
        val parser=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply {isLenient=false}
        runCatching {if(value.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}"))) parser.parse(value)?.let {if(it.time<=System.currentTimeMillis()) calendar.time=it}}
        val dialog=android.app.DatePickerDialog(context,{_,year,month,day ->
            onChange(String.format(Locale.US,"%04d-%02d-%02d",year,month+1,day));open=false
        },calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH))
        dialog.setTitle(label)
        dialog.datePicker.maxDate=System.currentTimeMillis()
        dialog.setOnDismissListener {open=false}
        dialog.show()
        dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setTextColor(android.graphics.Color.rgb(16,163,99))
        onDispose {dialog.dismiss()}
    }
}
@Composable internal fun animalFailureText(error: AuthFailure): String=stringResource(when {
    error.code=="UNCERTAIN"->R.string.animal_uncertain
    error.code=="DUPLICATE_ANIMAL_CODE"->R.string.animal_duplicate
    error.code=="INVENTORY_LIMIT_REACHED"->R.string.animal_limit
    error.status==400->R.string.animal_validation
    error.status==403 || error.problem==AuthProblem.FORBIDDEN->R.string.animal_forbidden
    error.status==404->R.string.animal_missing
    error.problem==AuthProblem.NETWORK->R.string.animal_network
    else->R.string.animal_error
})
@Composable fun AnimalRegistrationScreen(state: LivestockState,vm: LivestockViewModel,back: ()->Unit,created: (String)->Unit) {
    var farm by remember {mutableStateOf(state.filter.farmId.orEmpty())};var species by remember {mutableStateOf("")}
    var sex by remember {mutableStateOf(AnimalSex.DESCONOCIDO)}
    var code by remember {mutableStateOf("")};var name by remember {mutableStateOf("")};var breed by remember {mutableStateOf("")};var birth by remember {mutableStateOf("")}
    var attempted by remember {mutableStateOf(false)}
    var addFarm by remember {mutableStateOf(false)}
    var farmName by remember {mutableStateOf("")};var farmLocation by remember {mutableStateOf("")}
    LaunchedEffect(state.createdFarm?.id) {if(addFarm) state.createdFarm?.let {farm=it.id;addFarm=false;vm.clearCreation()}}
    val input=NewAnimal(farm,species,sex,code.takeIf(String::isNotBlank),name.takeIf(String::isNotBlank),breed.takeIf(String::isNotBlank),birth.takeIf(String::isNotBlank))
    val errors=if(attempted) animalInputErrors(input) else emptySet()
    LaunchedEffect(input) {vm.clearCorrectedAnimalInputError(input)}
    LaunchedEffect(state.createdAnimal?.id) {state.createdAnimal?.let {vm.consumeAnimal();created(it.id)}}
    AnimalPage(stringResource(R.string.animal_title),back,state.registering,subtitle=stringResource(R.string.animal_form_hint)) {
        val speciesName=state.species.value?.firstOrNull {it.id==species}?.name.orEmpty()
        Surface(Modifier.fillMaxWidth().testTag("animal_preview"),shape=RoundedCornerShape(15.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
            Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(15.dp)) {
                AnimalSpeciesImage(speciesName)
                Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
                    LsText(stringResource(R.string.animal_preview),size=12,color=LivestockVisuals.Secondary)
                    LsText(speciesName.ifBlank {stringResource(R.string.animal_choose_species)},size=13,bold=true)
                }
            }
        }
        if(state.farms.loading || state.species.loading || state.capacity.loading) LsLoading(stringResource(R.string.ls_loading))
        if(state.farms.failed || state.species.failed || state.capacity.failed) {
            LsNotice(stringResource(R.string.ls_partial_error),true)
            LsAction(stringResource(R.string.ls_retry),vm::refreshFoundation)
        }
        AnimalSelect(stringResource(R.string.animal_farm),state.farms.value?.firstOrNull {it.id==farm}?.name ?: stringResource(R.string.animal_select),state.farms.value.orEmpty().map {it.id to it.name},!state.registering,"animal_farm") {farm=it}
        if(state.farms.value?.isEmpty()==true) LsText(stringResource(R.string.ls_no_farms),size=11)
        TextButton(onClick={vm.clearCreation();addFarm=true},enabled=!state.registering) {LsText(stringResource(R.string.ls_add_farm),size=12,color=PrimaryGreen)}
        if("farmId" in errors) LsText(stringResource(R.string.animal_invalid),color=Color.Red,size=11)
        AnimalField(code,{code=it},stringResource(R.string.animal_code),stringResource(R.string.animal_auto),"animal_code",if("code" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        val sexes=listOf(AnimalSex.MACHO to stringResource(R.string.animal_male),AnimalSex.HEMBRA to stringResource(R.string.animal_female),AnimalSex.DESCONOCIDO to stringResource(R.string.animal_unspecified))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_species),state.species.value?.firstOrNull {it.id==species}?.name ?: stringResource(R.string.animal_select),state.species.value.orEmpty().map {it.id to it.name},!state.registering,"animal_species") {species=it}}
            Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_sex),sexes.first {it.first==sex}.second,sexes.map {it.first.name to it.second},!state.registering,"animal_sex") {sex=AnimalSex.valueOf(it)}}
        }
        if("speciesId" in errors) LsText(stringResource(R.string.animal_invalid),color=Color.Red,size=11)
        AnimalField(name,{name=it},stringResource(R.string.animal_name),"", "animal_name",if("name" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        AnimalField(breed,{breed=it},stringResource(R.string.animal_breed),"", "animal_breed",if("breed" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        AnimalBirthField(birth,{birth=it},if("birthDate" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        LsText(stringResource(R.string.animal_capacity),size=11,color=LivestockVisuals.Secondary)
        state.capacity.value?.let {LsText(stringResource(R.string.ls_capacity_compact,it.activeAnimals,it.allowedAnimals,it.remaining),size=11,color=LivestockVisuals.Secondary)}
        state.animalError?.let {LsNotice(animalFailureText(it),true)}
        if(state.animalError?.code=="UNCERTAIN") LsAction(stringResource(R.string.animal_acknowledge),vm::acknowledgeAnimalUncertainty)
        if(state.registering) LsLoading(stringResource(R.string.ls_loading))
        LsAction(stringResource(R.string.animal_save),{attempted=true;vm.createAnimal(input)},Modifier.fillMaxWidth().testTag("animal_submit"),primary=true,enabled=!state.registering && state.animalError?.code!="UNCERTAIN")
    }
    if(addFarm) LsModal(stringResource(R.string.ls_add_farm),{if(!state.saving) addFarm=false},content={
        LsField(farmName,{farmName=it},stringResource(R.string.ls_name),stringResource(R.string.ls_name_help),"farm_name",null,!state.saving)
        LsField(farmLocation,{farmLocation=it},stringResource(R.string.ls_location),stringResource(R.string.ls_location_help),"farm_location",null,!state.saving)
        if(state.saving) LsLoading(stringResource(R.string.ls_saving))
        state.saveError?.let {LsNotice(stringResource(if(it=="UNCERTAIN") R.string.ls_uncertain else R.string.ls_form_error),true)}
    },actions={
        LsAction(stringResource(R.string.ls_cancel),{addFarm=false},Modifier.weight(1f),enabled=!state.saving)
        LsAction(stringResource(R.string.ls_save),{vm.createFarm(farmName,farmLocation)},Modifier.weight(1f),primary=true,
            enabled=!state.saving && state.saveError!="UNCERTAIN" && farmName.isNotBlank() && farmName.length<=150 && farmLocation.length<=255)
    })
}
@Composable internal fun AnimalSpeciesImage(species: String,photoUrl: String?=null,edge: Int=73) {
    Box(Modifier.size(edge.dp).background(LivestockVisuals.Mint,RoundedCornerShape(24.dp)),contentAlignment=Alignment.Center) {
        val placeholder=painterResource(livestockSpeciesIcon(species))
        val remote=photoUrl?.takeIf {url->runCatching {val uri=java.net.URI(url);uri.scheme=="https" && uri.host!=null && uri.userInfo==null}.getOrDefault(false)}
        if(remote!=null) AsyncImage(model=remote,contentDescription=null,placeholder=placeholder,error=placeholder,modifier=Modifier.size((edge-20).dp))
        else Icon(placeholder,null,Modifier.size((edge-20).dp),tint=Color.Unspecified)
    }
}
@Composable private fun AnimalOptions(enabled: Boolean,canDeactivate: Boolean,onEdit: ()->Unit,onDeactivate: ()->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Box {
        AnimalNavigationButton(R.drawable.animal_more_official,stringResource(R.string.animal_more),enabled) {expanded=true}
        val offset=with(androidx.compose.ui.platform.LocalDensity.current) {48.dp.roundToPx()}
        if(expanded) androidx.compose.ui.window.Popup(alignment=Alignment.TopEnd,offset=androidx.compose.ui.unit.IntOffset(0,offset),
            onDismissRequest={expanded=false},properties=PopupProperties(focusable=true)) {
            Surface(Modifier.width(152.dp).testTag("animal_options"),shape=RoundedCornerShape(14.dp),color=Color.White,
                border=BorderStroke(1.dp,LivestockVisuals.Border),shadowElevation=12.dp) { Column {
            DropdownMenuItem(onClick={expanded=false;onEdit()},enabled=enabled,modifier=Modifier.height(40.dp).testTag("animal_edit"),contentPadding=PaddingValues(horizontal=15.dp),
                leadingIcon={Icon(painterResource(R.drawable.animal_edit_official),null,Modifier.size(16.dp),tint=Color.Unspecified)},
                text={LsText(stringResource(R.string.animal_edit),size=10)})
            HorizontalDivider(color=LivestockVisuals.Border)
            DropdownMenuItem(onClick={expanded=false;onDeactivate()},enabled=enabled && canDeactivate,modifier=Modifier.height(40.dp).testTag("animal_deactivate"),contentPadding=PaddingValues(horizontal=15.dp),
                leadingIcon={Icon(painterResource(R.drawable.animal_deactivate_official),null,Modifier.size(16.dp),tint=Color.Unspecified)},
                text={LsText(stringResource(R.string.animal_deactivate),size=10,color=Color(0xFFDF6262))})
            } }
        }
    }
}
@Composable fun AnimalDetailScreen(state: LivestockState,back: ()->Unit,retry: ()->Unit,
    edit: ()->Unit={},deactivate: (AnimalDetail)->Unit={},reloadMutation: ()->Unit={},consumeMutation: ()->Unit={},readOnly: Boolean=false,observations: @Composable (String)->Unit={}) {
    var confirm by remember(state.animalDetailId) {mutableStateOf(false)}
    LaunchedEffect(state.mutation.completed) {if(state.mutation.completed=="DEACTIVATE") confirm=false}
    val current=state.animalDetail.value
    if(!readOnly && confirm && current!=null) AnimalDeactivationDialog(current,state.mutation,{if(!state.mutation.busy) confirm=false},{deactivate(current)})
    BackHandler(state.mutation.busy) {}
    AnimalPage(state.animalDetail.value?.let {it.name?.takeIf(String::isNotBlank) ?: it.code} ?: stringResource(R.string.animal_detail),back,
        subtitle=state.animalDetail.value?.let {it.code+" · "+it.species},centered=true,busy=state.mutation.busy,actions=if(state.animalDetail.value!=null && !readOnly) {{AnimalOptions(!state.mutation.busy,state.animalDetail.value.status==AnimalStatus.ACTIVO,edit) {consumeMutation();confirm=true}}} else null) {
        state.mutation.error?.let {LsNotice(animalMutationFailureText(it),true);LsAction(stringResource(R.string.animal_reload),reloadMutation)}
        if(state.mutation.completed=="DEACTIVATE") LsNotice(stringResource(R.string.animal_deactivated))
        if(state.animalDetail.loading) LsLoading(stringResource(R.string.ls_loading))
        state.animalDetailError?.let {LsNotice(animalFailureText(it),true);LsAction(stringResource(R.string.ls_retry),retry)}
        state.animalDetail.value?.let {animal ->
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
                Row(Modifier.padding(16.dp),horizontalArrangement=Arrangement.spacedBy(15.dp),verticalAlignment=Alignment.CenterVertically) {
                    AnimalSpeciesImage(animal.species,animal.photoUrl)
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(7.dp)) {
                        val active=animal.status==AnimalStatus.ACTIVO
                        val label=stringResource(when(animal.status) {AnimalStatus.ACTIVO->R.string.ls_badge_active;AnimalStatus.INACTIVO->R.string.ls_badge_inactive;AnimalStatus.VENDIDO->R.string.ls_badge_sold;else->R.string.ls_badge_dead})
                        LsText(label,Modifier.background(if(active) LivestockVisuals.Mint else LivestockVisuals.Pale,RoundedCornerShape(10.dp)).padding(horizontal=8.dp,vertical=4.dp),size=12,bold=true,color=if(active) PrimaryGreen else LivestockVisuals.Secondary)
                        LsText(animal.farmName,size=12,color=LivestockVisuals.Secondary)
                    }
                }
            }
            LsText(stringResource(R.string.animal_general),Modifier.padding(top=4.dp),bold=true)
            val sex=stringResource(when(animal.sex) {AnimalSex.MACHO->R.string.animal_male;AnimalSex.HEMBRA->R.string.animal_female;else->R.string.animal_unspecified})
            val fields=listOf(R.string.animal_species to animal.species,R.string.animal_sex to sex,R.string.animal_breed_label to animal.breed,R.string.animal_birth_label to animal.birthDate)
            Surface(Modifier.fillMaxWidth().testTag("animal_general_grid"),shape=RoundedCornerShape(16.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
                Column {
                    fields.chunked(2).forEachIndexed {index,row ->
                        if(index>0) HorizontalDivider(color=LivestockVisuals.Border)
                        Row(Modifier.height(IntrinsicSize.Min)) {
                            row.forEachIndexed {column,(label,value) ->
                                if(column>0) VerticalDivider(color=LivestockVisuals.Border)
                                Column(Modifier.weight(1f).padding(12.dp),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                                    LsText(stringResource(label),size=10,color=LivestockVisuals.Secondary)
                                    LsText(value?.takeIf(String::isNotBlank) ?: stringResource(R.string.animal_unknown),size=11,bold=true)
                                }
                            }
                        }
                    }
                }
            }
            observations(animal.id)
        }
    }
}
