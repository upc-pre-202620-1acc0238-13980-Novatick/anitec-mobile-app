package pe.novatick.anitec.features.livestock.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.compose.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.Profile
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.theme.*
import pe.novatick.anitec.ui.components.*
import pe.novatick.anitec.ui.navigation.RancherTab
import pe.novatick.anitec.ui.screens.home.RancherProfileScreen

@Composable fun LivestockShell(vm: LivestockViewModel,profile: Profile,onLogout: ()->Unit,linking: pe.novatick.anitec.features.linking.domain.LinkingRepository?=null) {
    val linkingVm=remember(linking,profile.id) {linking?.let {pe.novatick.anitec.features.linking.presentation.LinkingViewModel(it,profile.role)}}
    DisposableEffect(linkingVm){linkingVm?.refresh();onDispose {linkingVm?.close()}}
    val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState()
    val tab=when(entry?.destination?.route) { "cattle","animal_register","animal/{id}","animal/{id}/edit","animal/{id}/observations","animal/{id}/observation/new"->RancherTab.CATTLE;"profile"->RancherTab.PROFILE;"vets","invite_vet"->RancherTab.VETS;else->RancherTab.HOME }
    val obs by vm.observations.uiState.collectAsState();val state by vm.uiState.collectAsState();val type=MaterialTheme.typography
    val labels=mapOf(RancherTab.HOME to stringResource(R.string.ls_tab_home),RancherTab.CATTLE to stringResource(R.string.ls_tab_cattle),
        RancherTab.VETS to stringResource(R.string.ls_tab_vets),RancherTab.PROFILE to stringResource(R.string.ls_tab_profile))
    MaterialTheme(colorScheme=MaterialTheme.colorScheme.copy(primary=PrimaryGreen,onPrimary=Color.White,surface=Color.White),
        typography=type.copy(bodyLarge=type.bodyLarge.copy(fontFamily=Poppins),bodyMedium=type.bodyMedium.copy(fontFamily=Poppins),labelLarge=type.labelLarge.copy(fontFamily=Poppins))) {
        Scaffold(containerColor=Color.White,bottomBar={RancherBottomBar(tab,labels) { value ->
            if(state.registering || state.mutation.busy || obs.busy) return@RancherBottomBar
            nav.navigate(when(value) { RancherTab.HOME->"home";RancherTab.CATTLE->"cattle";RancherTab.PROFILE->"profile";RancherTab.VETS->"vets" }) { launchSingleTop=true;popUpTo("home") }
        }}) { padding -> Box(Modifier.padding(padding).consumeWindowInsets(padding)) { NavHost(nav,startDestination="home") {
            composable("home") { pe.novatick.anitec.RancherHomeScreen(onNavigateToCattle={nav.navigate("cattle")},onNavigateToLinkedVets={nav.navigate("vets")},
                onNavigateToRegisterAnimal={},onNavigateToInviteVets={nav.navigate("invite_vet")},onNavigateToVisits={},onNavigateToVisitDetail={},onNavigateToIndicationDetail={},
                profileName=profile.name,profileInitials=livestockInitials(profile.name,profile.lastName),
                livestockContent={ LivestockHomeSummary(state.capacity,onInvite={nav.navigate("invite_vet")},onRegister={vm.beginAnimal();nav.navigate("animal_register")},onRetry=vm::refreshFoundation) {nav.navigate("cattle")} }) }
            composable("cattle") {LivestockInventoryScreen(state,vm,onRegister={vm.beginAnimal();nav.navigate("animal_register")},onAnimal={nav.navigate("animal/$it")})}
            composable("animal_register") {AnimalRegistrationScreen(state,vm,{nav.popBackStack()}) {nav.navigate("animal/$it") {popUpTo("animal_register") {inclusive=true}}}}
            composable("animal/{id}") {back -> val id=back.arguments?.getString("id").orEmpty()
                LaunchedEffect(id) {vm.readAnimal(id);vm.observations.load(id)}
                val selected=if(state.animalDetailId==id) state else state.copy(animalDetail=ReadState(loading=true),animalDetailError=null)
                AnimalDetailScreen(selected,{vm.consumeMutation();nav.popBackStack()},{vm.readAnimal(id)},
                    edit={vm.beginMutation();nav.navigate("animal/$id/edit")},deactivate=vm::deactivateAnimal,
                    reloadMutation={vm.reloadMutation(id)},consumeMutation=vm::consumeMutation,observations={animalId ->
                        ObservationSection(animalId,obs,vm.observations,{nav.navigate("animal/$id/observations")},{vm.observations.begin();nav.navigate("animal/$id/observation/new")})
                    })
            }
            composable("animal/{id}/edit") {back -> val id=back.arguments?.getString("id").orEmpty()
                LaunchedEffect(id) {if(state.animalDetailId!=id) vm.readAnimal(id);vm.refreshFoundation()}
                AnimalEditScreen(state,vm,{vm.consumeMutation();nav.popBackStack()}) {vm.consumeMutation();nav.popBackStack()}
            }
            composable("animal/{id}/observations") {entry -> val id=entry.arguments?.getString("id").orEmpty()
                LaunchedEffect(id) {vm.observations.load(id)}
                ObservationListScreen(id,obs,vm.observations,{nav.popBackStack()}) {vm.observations.begin();nav.navigate("animal/$id/observation/new")}
            }
            composable("animal/{id}/observation/new") {entry -> val id=entry.arguments?.getString("id").orEmpty()
                LaunchedEffect(id) {if(state.animalDetailId!=id) vm.readAnimal(id)}
                ObservationForm(state.animalDetail.value?.takeIf {it.id==id},obs,vm.observations,{nav.popBackStack()}) {vm.readAnimal(id);nav.popBackStack()}
            }
            composable("profile") {RancherProfileScreen(userName=profile.name+" "+profile.lastName,userEmail=profile.email,onLogoutClick=onLogout)}
            composable("vets") {linkingVm?.let {pe.novatick.anitec.features.linking.presentation.RancherLinksScreen(it,{nav.navigate("invite_vet")})}}
            composable("invite_vet") {linkingVm?.let {pe.novatick.anitec.features.linking.presentation.InviteVetScreen(it,{nav.popBackStack()})}}
        } } }
    }
}
@Composable private fun CapacitySummary(value: ReadState<Capacity>,showError: Boolean=true) {
    if(value.loading) LsLoading(stringResource(R.string.ls_loading))
    value.value?.let {
        val description=stringResource(R.string.ls_capacity,it.activeAnimals,it.allowedAnimals,it.remaining)
        LsText(stringResource(R.string.ls_capacity_compact,it.activeAnimals,it.allowedAnimals,it.remaining),
            Modifier.testTag("livestock_capacity").semantics {contentDescription=description},size=11,color=LivestockVisuals.Secondary)
    }
    if(showError && value.failed) LsNotice(stringResource(if(value.stale) R.string.ls_stale else R.string.ls_error),true)
}
@Composable internal fun LivestockHomeSummary(value: ReadState<Capacity>,onRetry: ()->Unit={},onRegister: ()->Unit={},onInvite: ()->Unit={},onInventory: ()->Unit) {
    value.value?.let { capacity ->
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Metric(capacity.activeAnimals,stringResource(R.string.ls_metric_active),R.drawable.ic_cow,Modifier.weight(1f).testTag("home_inventory_active"),onInventory)
            Metric(capacity.remaining,stringResource(R.string.ls_metric_remaining),R.drawable.ic_plus,Modifier.weight(1f),onInventory)
        }
    }
    if(value.loading) LsLoading(stringResource(R.string.ls_loading))
    if(value.failed) {
        LsNotice(stringResource(R.string.ls_partial_error),true)
        LsReadIssue(stringResource(R.string.ls_capacity_error),value.stale,onRetry)
    }
    LivestockHomeSections(onRegister,onInvite)

}
@Composable private fun Metric(value: Long,label: String,icon: Int,modifier: Modifier,onClick: ()->Unit) {
    Surface(modifier.heightIn(min=97.dp).clickable(onClick=onClick),shape=RoundedCornerShape(18.dp),color=Color.White,
        border=BorderStroke(1.dp,Color(0xFFEDF2EF)),shadowElevation=6.dp) {
        Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(40.dp).background(LivestockVisuals.Mint,RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center) {
                Icon(painterResource(icon),null,tint=PrimaryGreen,modifier=Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f)) {LsText(value.toString(),size=24,bold=true);LsText(label,size=11,color=LivestockVisuals.Secondary)}
            Icon(painterResource(R.drawable.ls_chevron_official),null,tint=Color.Unspecified,modifier=Modifier.size(8.25.dp,14.5.dp))
        }
    }
}
@Composable private fun Choice(label: String,tag: String,choices: List<Pair<String,String?>>,onSelect: (String?)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Box {OutlinedButton(onClick={expanded=true},modifier=Modifier.heightIn(min=44.dp).testTag(tag),shape=RoundedCornerShape(14.dp),
        border=BorderStroke(1.dp,LivestockVisuals.Border),contentPadding=PaddingValues(horizontal=12.dp,vertical=8.dp),
        colors=ButtonDefaults.outlinedButtonColors(containerColor=LivestockVisuals.Pale,contentColor=PrimaryGreen)) {
        Text(label,fontFamily=Poppins,fontSize=12.sp,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis)
        Spacer(Modifier.width(6.dp))
        Box(Modifier.size(14.5.dp,8.25.dp),contentAlignment=Alignment.Center) {
            Icon(painterResource(R.drawable.ls_chevron_official),null,tint=PrimaryGreen,modifier=Modifier.requiredSize(8.25.dp,14.5.dp).rotate(90f))
        }
    }
        DropdownMenu(expanded,onDismissRequest={expanded=false}) { choices.forEach { (text,id) ->
            DropdownMenuItem(text={LsText(text)},onClick={expanded=false;onSelect(id)})
        } }
    }
}
@Composable private fun stateName(status: AnimalStatus): String = stringResource(when(status) {
    AnimalStatus.ACTIVO->R.string.ls_active;AnimalStatus.INACTIVO->R.string.ls_inactive;AnimalStatus.VENDIDO->R.string.ls_sold;AnimalStatus.FALLECIDO->R.string.ls_dead
})
@Composable internal fun AnimalRow(animal: AnimalSummary,speciesName: String=animal.species,onClick: (()->Unit)?=null) {
    val background=when(animal.status) {AnimalStatus.ACTIVO->LivestockVisuals.Mint;AnimalStatus.INACTIVO->LivestockVisuals.Pale;AnimalStatus.VENDIDO->Color(0xFFFFF7DF);AnimalStatus.FALLECIDO->Color(0xFFFFF0F0)}
    val foreground=when(animal.status) {AnimalStatus.ACTIVO->PrimaryGreen;AnimalStatus.INACTIVO->LivestockVisuals.Secondary;AnimalStatus.VENDIDO->Color(0xFF806014);AnimalStatus.FALLECIDO->Color(0xFFAE4141)}
    Surface(Modifier.fillMaxWidth().testTag("animal_${animal.id}").then(if(onClick!=null) Modifier.clickable(onClick=onClick) else Modifier),shape=RoundedCornerShape(15.dp),color=Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)) {
        Row(Modifier.heightIn(min=76.dp).padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(41.dp).background(LivestockVisuals.Mint,RoundedCornerShape(12.dp)),contentAlignment=Alignment.Center) {
                Icon(painterResource(livestockSpeciesIcon(speciesName)),null,tint=Color.Unspecified,modifier=Modifier.size(29.dp))
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)) {
                Text(animal.name?.takeIf(String::isNotBlank) ?: animal.code,fontFamily=Poppins,fontSize=13.sp,fontWeight=FontWeight.SemiBold,
                    color=LivestockVisuals.Ink,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(animal.code+" - "+animal.species,fontFamily=Poppins,fontSize=11.sp,color=LivestockVisuals.Secondary,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            LsText(stringResource(when(animal.status) { AnimalStatus.ACTIVO->R.string.ls_badge_active;AnimalStatus.INACTIVO->R.string.ls_badge_inactive;AnimalStatus.VENDIDO->R.string.ls_badge_sold;AnimalStatus.FALLECIDO->R.string.ls_badge_dead }),Modifier.background(background,RoundedCornerShape(10.dp)).padding(horizontal=8.dp,vertical=5.dp),size=10,bold=true,color=foreground)
            if(onClick!=null) Icon(painterResource(R.drawable.ls_chevron_official),null,tint=Color.Unspecified,modifier=Modifier.size(8.25.dp,14.5.dp))
        }
    }
}
@Composable fun LivestockInventoryScreen(state: LivestockState,vm: LivestockViewModel,onRegister: ()->Unit={},onAnimal: ((String)->Unit)?=null) {
    val unavailable=rememberLivestockUnavailableAction()
    var farmsOpen by remember {mutableStateOf(false)};var createOpen by remember {mutableStateOf(false)}
    var name by remember {mutableStateOf("")};var location by remember {mutableStateOf("")};var attempted by remember {mutableStateOf(false)}
    LaunchedEffect(state.createdFarm) {if(state.createdFarm!=null) {createOpen=false;farmsOpen=false;name="";location="";attempted=false;vm.clearCreation()}}
    val statuses=listOf(stringResource(R.string.ls_all) to null)+AnimalStatus.entries.map {stateName(it) to it.name}
    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(30.dp).clip(CattleHeaderShape).background(PrimaryGreenAccent))
        Column(Modifier.padding(horizontal=20.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.ls_inventory),modifier=Modifier.testTag("livestock_title"),fontFamily=Poppins,fontSize=24.sp,lineHeight=26.25.sp,fontWeight=FontWeight.Bold,color=PrimaryGreen)
                    state.inventory.value?.let { LsText(pluralStringResource(if(state.filter.farmId==null) R.plurals.ls_active_global_count else R.plurals.ls_active_farm_count,if(it.totalActive==1L) 1 else 2,it.totalActive),size=12,color=LivestockVisuals.Secondary) }
                }
                TextButton(onClick={vm.refreshFoundation();vm.filter(state.filter)}) {LsText(stringResource(R.string.ls_refresh),size=11,color=PrimaryGreen,bold=true)}
            }
            if(state.capacity.failed || state.farms.failed || state.species.failed || state.selectedFarm.failed || state.inventory.failed)
                LsNotice(stringResource(R.string.ls_partial_error),true)
            CapacitySummary(state.capacity,showError=false)
            if(state.capacity.failed) LsReadIssue(stringResource(R.string.ls_capacity_error),state.capacity.stale,vm::refreshFoundation)
            LsSearch(state.filter.search) {vm.filter(state.filter.copy(search=it),true)}
            LsAction(stringResource(R.string.ls_register_animal),onRegister,Modifier.fillMaxWidth(),primary=true,icon=R.drawable.ls_plus_official)
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                Box(Modifier.weight(1f)) {Choice(state.farms.value?.firstOrNull {it.id==state.filter.farmId}?.name ?: stringResource(if(state.filter.farmId==null) R.string.ls_all_farms else R.string.ls_selected_farm),
                    "farm_filter",listOf(stringResource(R.string.ls_all_farms) to null)+state.farms.value.orEmpty().map {it.name to it.id},vm::selectFarm)}
                TextButton(onClick={farmsOpen=true},modifier=Modifier.testTag("my_farms")) {LsText(stringResource(R.string.ls_farms),size=12,bold=true,color=PrimaryGreen)}
            }
            if(state.farms.loading || state.species.loading || state.selectedFarm.loading) LsLoading(stringResource(R.string.ls_loading))
            if(state.farms.failed) LsReadIssue(stringResource(R.string.ls_farms_error),state.farms.stale,vm::refreshFoundation)
            if(state.species.failed) LsReadIssue(stringResource(R.string.ls_species_error),state.species.stale,vm::refreshFoundation)
            if(state.selectedFarm.failed) LsReadIssue(stringResource(R.string.ls_farm_error),state.selectedFarm.stale) {vm.selectFarm(state.filter.farmId)}
            if(state.farms.value?.isEmpty()==true) LsNotice(stringResource(R.string.ls_no_farms))
            state.selectedFarm.value?.let {LsText(it.location?.takeIf(String::isNotBlank) ?: stringResource(R.string.ls_no_location),size=11,color=LivestockVisuals.Secondary)}
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag("species_filters"),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                val choices=listOf(stringResource(R.string.ls_all_species) to null)+state.species.value.orEmpty().map {it.name to it.id}
                choices.forEach { (label,id)->FilterChip(state.filter.species==id,{vm.filter(state.filter.copy(species=id))},label={LsText(label,size=11,bold=true,color=if(state.filter.species==id) Color.White else LivestockVisuals.Secondary)},
                    modifier=Modifier.testTag("species_${id ?: "all"}"),shape=RoundedCornerShape(20.dp),border=null,
                    colors=FilterChipDefaults.filterChipColors(containerColor=LivestockVisuals.Pale,selectedContainerColor=Color(0xFF34C083))) }
            }
            if(state.species.value?.isEmpty()==true) LsNotice(stringResource(R.string.ls_no_species))
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) {Choice(statuses.first {it.second==state.filter.status?.name}.first,"status_filter",statuses) {vm.filter(state.filter.copy(status=it?.let(AnimalStatus::valueOf)))}}
                state.inventory.value?.let {LsText(pluralStringResource(R.plurals.ls_result_count,it.content.size,it.content.size),size=11,color=LivestockVisuals.Secondary)}
            }
            if(state.inventory.loading) LsLoading(stringResource(R.string.ls_loading_inventory))
            if(state.inventory.failed) LsReadIssue(stringResource(R.string.ls_inventory_error),state.inventory.stale) {vm.filter(state.filter)}
            state.inventory.value?.let { data ->
                if(data.content.isEmpty()) {
                    val filtered=state.filter.search.isNotBlank() || state.filter.species!=null || state.filter.status!=AnimalStatus.ACTIVO
                    LsEmpty(stringResource(if(filtered) R.string.ls_no_results else R.string.ls_no_animals),stringResource(if(filtered) R.string.ls_empty_search_hint else R.string.ls_empty_inventory_hint))
                    if(filtered) TextButton(onClick={vm.filter(InventoryFilter(farmId=state.filter.farmId))},modifier=Modifier.align(Alignment.CenterHorizontally)) {LsText(stringResource(R.string.ls_clear_filters),color=PrimaryGreen,bold=true)}
                }
                data.content.forEach { animal -> AnimalRow(animal,state.species.value?.firstOrNull {it.id==animal.speciesId}?.name ?: animal.species,onAnimal?.let {callback->{callback(animal.id)}}) }
            }
        }
    }
    if(farmsOpen) LsModal(stringResource(R.string.ls_farms),{farmsOpen=false},content={
        if(state.farms.loading) LsLoading(stringResource(R.string.ls_loading))
        if(state.farms.failed) LsNotice(stringResource(R.string.ls_error),true)
        if(state.farms.value?.isEmpty()==true) LsEmpty(stringResource(R.string.ls_no_farms),stringResource(R.string.ls_farm_hint))
        state.farms.value.orEmpty().forEach { farm -> Surface(Modifier.fillMaxWidth().testTag("farm_${farm.id}").selectable(state.filter.farmId==farm.id,onClick={vm.selectFarm(farm.id);farmsOpen=false}),
            shape=RoundedCornerShape(14.dp),color=if(state.filter.farmId==farm.id) LivestockVisuals.Mint else Color.White,border=BorderStroke(1.dp,LivestockVisuals.Border)) {
            Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(R.drawable.ic_farm),null,tint=PrimaryGreen,modifier=Modifier.size(24.dp))
                Column(Modifier.weight(1f)) {LsText(farm.name,bold=true);LsText(farm.location?.takeIf(String::isNotBlank) ?: stringResource(R.string.ls_no_location),size=11,color=LivestockVisuals.Secondary)}
            }
        } }
    },actions={
        LsAction(stringResource(R.string.ls_close),{farmsOpen=false},Modifier.weight(1f))
        LsAction(stringResource(R.string.ls_add_farm),{vm.clearCreation();farmsOpen=false;createOpen=true;attempted=false},Modifier.weight(1f),primary=true)
    })
    if(createOpen) LsModal(stringResource(R.string.ls_add_farm),{if(!state.saving) {createOpen=false;farmsOpen=true}},content={
        LsText(stringResource(R.string.ls_farm_hint),size=12,color=LivestockVisuals.Secondary)
        LsField(name,{name=it},stringResource(R.string.ls_name),stringResource(R.string.ls_name_help),"farm_name",
            if(attempted && (name.isBlank() || name.length>150)) stringResource(R.string.ls_name_error) else null,!state.saving)
        LsField(location,{location=it},stringResource(R.string.ls_location),stringResource(R.string.ls_location_help),"farm_location",
            if(attempted && location.length>255) stringResource(R.string.ls_location_error) else null,!state.saving)
        if(state.saving) LsLoading(stringResource(R.string.ls_saving))
        state.saveError?.let {LsNotice(stringResource(if(it=="UNCERTAIN") R.string.ls_uncertain else R.string.ls_form_error),true)}
    },actions={
        LsAction(stringResource(R.string.ls_cancel),{createOpen=false;farmsOpen=true},Modifier.weight(1f),enabled=!state.saving)
        LsAction(stringResource(R.string.ls_save),{attempted=true;if(name.isNotBlank() && name.length<=150 && location.length<=255) vm.createFarm(name,location)},Modifier.weight(1f),primary=true,enabled=!state.saving && state.saveError!="UNCERTAIN")
    })
}
