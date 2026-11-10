package pe.novatick.anitec.features.livestock.presentation

import androidx.activity.compose.BackHandler
import coil.compose.AsyncImage
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.dp
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.components.CattleHeaderShape
import pe.novatick.anitec.ui.theme.PrimaryGreen

@Composable private fun AnimalPage(title: String,back: ()->Unit,busy: Boolean=false,content: @Composable ColumnScope.()->Unit) {
    BackHandler(busy) {}
    Column(Modifier.fillMaxSize().background(Color.White).imePadding().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(30.dp).clip(CattleHeaderShape).background(pe.novatick.anitec.ui.theme.PrimaryGreenAccent))
        Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                IconButton(onClick=back,enabled=!busy) {Icon(painterResource(R.drawable.ic_arrow_left),stringResource(R.string.animal_back),tint=PrimaryGreen)}
                LsText(title,size=21,bold=true,color=PrimaryGreen)
            }
            content()
        }
    }
}
@Composable private fun AnimalSelect(label: String,value: String,choices: List<Pair<String,String>>,enabled: Boolean,onSelect: (String)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
        LsText(label,size=12,bold=true)
        Box {
            OutlinedButton(onClick={expanded=true},enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=47.dp),shape=RoundedCornerShape(13.dp),
                colors=ButtonDefaults.outlinedButtonColors(containerColor=LivestockVisuals.Pale,contentColor=LivestockVisuals.Ink),border=BorderStroke(1.dp,LivestockVisuals.Border)) {
                LsText(value,Modifier.weight(1f),size=12)
                Box(Modifier.size(14.5.dp,8.25.dp),contentAlignment=Alignment.Center) {
                    Icon(painterResource(R.drawable.ls_chevron_official),null,Modifier.requiredSize(8.25.dp,14.5.dp).rotate(90f),tint=PrimaryGreen)
                }
            }
            DropdownMenu(expanded,{expanded=false}) {choices.forEach { (id,text)->DropdownMenuItem(text={LsText(text)},onClick={expanded=false;onSelect(id)})}}
        }
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
    LaunchedEffect(state.createdAnimal?.id) {state.createdAnimal?.let {vm.consumeAnimal();created(it.id)}}
    AnimalPage(stringResource(R.string.animal_title),back,state.registering) {
        Icon(painterResource(livestockSpeciesIcon(state.species.value?.firstOrNull {it.id==species}?.name.orEmpty())),null,Modifier.align(Alignment.CenterHorizontally).size(73.dp),tint=Color.Unspecified)
        LsText(stringResource(R.string.animal_photo),size=11,color=LivestockVisuals.Secondary)
        if(state.farms.loading || state.species.loading || state.capacity.loading) LsLoading(stringResource(R.string.ls_loading))
        if(state.farms.failed || state.species.failed || state.capacity.failed) {
            LsNotice(stringResource(R.string.ls_partial_error),true)
            LsAction(stringResource(R.string.ls_retry),vm::refreshFoundation)
        }
        AnimalSelect(stringResource(R.string.animal_farm),state.farms.value?.firstOrNull {it.id==farm}?.name ?: stringResource(R.string.animal_select),state.farms.value.orEmpty().map {it.id to it.name},!state.registering) {farm=it}
        if(state.farms.value?.isEmpty()==true) LsText(stringResource(R.string.ls_no_farms),size=11)
        TextButton(onClick={vm.clearCreation();addFarm=true},enabled=!state.registering) {LsText(stringResource(R.string.ls_add_farm),size=12,color=PrimaryGreen)}
        if("farmId" in errors) LsText(stringResource(R.string.animal_invalid),color=Color.Red,size=11)
        LsField(code,{code=it},stringResource(R.string.animal_code),stringResource(R.string.animal_auto),"animal_code",if("code" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        val sexes=listOf(AnimalSex.MACHO to stringResource(R.string.animal_male),AnimalSex.HEMBRA to stringResource(R.string.animal_female),AnimalSex.DESCONOCIDO to stringResource(R.string.animal_unspecified))
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_species),state.species.value?.firstOrNull {it.id==species}?.name ?: stringResource(R.string.animal_select),state.species.value.orEmpty().map {it.id to it.name},!state.registering) {species=it}}
            Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_sex),sexes.first {it.first==sex}.second,sexes.map {it.first.name to it.second},!state.registering) {sex=AnimalSex.valueOf(it)}}
        }
        if("speciesId" in errors) LsText(stringResource(R.string.animal_invalid),color=Color.Red,size=11)
        LsField(name,{name=it},stringResource(R.string.animal_name),"", "animal_name",if("name" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        LsField(breed,{breed=it},stringResource(R.string.animal_breed),"", "animal_breed",if("breed" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
        LsField(birth,{birth=it},stringResource(R.string.animal_birth),stringResource(R.string.animal_date),"animal_birth",if("birthDate" in errors) stringResource(R.string.animal_invalid) else null,!state.registering)
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
@Composable fun AnimalDetailScreen(state: LivestockState,back: ()->Unit,retry: ()->Unit) {
    AnimalPage(state.animalDetail.value?.let {it.name ?: it.code} ?: stringResource(R.string.animal_detail),back) {
        if(state.animalDetail.loading) LsLoading(stringResource(R.string.ls_loading))
        state.animalDetailError?.let {LsNotice(animalFailureText(it),true);LsAction(stringResource(R.string.ls_retry),retry)}
        state.animalDetail.value?.let {animal ->
            LsText(animal.code+" - "+animal.species,color=LivestockVisuals.Secondary)
            Surface(shape=RoundedCornerShape(18.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
                Row(Modifier.fillMaxWidth().padding(16.dp),horizontalArrangement=Arrangement.spacedBy(15.dp)) {
                    val placeholder=painterResource(livestockSpeciesIcon(animal.species))
                    val remote=animal.photoUrl?.takeIf {url->runCatching {val uri=java.net.URI(url);uri.scheme=="https" && uri.host!=null && uri.userInfo==null}.getOrDefault(false)}
                    if(remote!=null) AsyncImage(model=remote,contentDescription=null,placeholder=placeholder,error=placeholder,modifier=Modifier.size(73.dp))
                    else Icon(placeholder,null,Modifier.size(73.dp),tint=Color.Unspecified)
                    Column {LsText(stringResource(when(animal.status) {AnimalStatus.ACTIVO->R.string.ls_badge_active;AnimalStatus.INACTIVO->R.string.ls_badge_inactive;AnimalStatus.VENDIDO->R.string.ls_badge_sold;else->R.string.ls_badge_dead}),bold=true,color=PrimaryGreen);LsText(animal.farmName,size=12)}
                }
            }
            LsText(stringResource(R.string.animal_general),bold=true)
            val sex=stringResource(when(animal.sex) {AnimalSex.MACHO->R.string.animal_male;AnimalSex.HEMBRA->R.string.animal_female;else->R.string.animal_unspecified})
            val fields=listOf(R.string.animal_species to animal.species,R.string.animal_sex to sex,R.string.animal_breed to animal.breed,R.string.animal_birth to animal.birthDate)
            fields.chunked(2).forEach {row ->
                Row(horizontalArrangement=Arrangement.spacedBy(1.dp)) {row.forEach { (label,value)->
                    Surface(Modifier.weight(1f),shape=RoundedCornerShape(12.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {LsText(stringResource(label),size=10,color=LivestockVisuals.Secondary);LsText(value ?: stringResource(R.string.animal_unknown),size=12,bold=true)}
                    }
                }}
            }
        }
    }
}
