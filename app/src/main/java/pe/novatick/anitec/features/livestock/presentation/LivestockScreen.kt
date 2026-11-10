package pe.novatick.anitec.features.livestock.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.*
import androidx.navigation.compose.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.Profile
import pe.novatick.anitec.features.auth.presentation.AuthMessage
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.theme.*
import pe.novatick.anitec.ui.components.*
import pe.novatick.anitec.ui.navigation.RancherTab
import pe.novatick.anitec.ui.screens.home.RancherProfileScreen

@Composable fun LivestockShell(vm: LivestockViewModel,profile: Profile,onLogout: ()->Unit) {
    val nav=rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val tab=when(entry?.destination?.route) { "cattle"->RancherTab.CATTLE;"profile"->RancherTab.PROFILE;"vets"->RancherTab.VETS;else->RancherTab.HOME }
    val state by vm.uiState.collectAsState()
    val typography=MaterialTheme.typography
    MaterialTheme(colorScheme=MaterialTheme.colorScheme.copy(primary=PrimaryGreen,onPrimary=androidx.compose.ui.graphics.Color.White),
        typography=typography.copy(bodyLarge=typography.bodyLarge.copy(fontFamily=Poppins),bodyMedium=typography.bodyMedium.copy(fontFamily=Poppins),labelLarge=typography.labelLarge.copy(fontFamily=Poppins))) {
    Scaffold(bottomBar={ RancherBottomBar(tab) { value ->
        val route=when(value) { RancherTab.HOME->"home";RancherTab.CATTLE->"cattle";RancherTab.PROFILE->"profile";RancherTab.VETS->"vets" }
        nav.navigate(route) { launchSingleTop=true;popUpTo("home") }
    } }) { padding -> Box(Modifier.padding(padding)) {
        NavHost(nav,startDestination="home") {
            composable("home") { pe.novatick.anitec.RancherHomeScreen(
                onNavigateToCattle={nav.navigate("cattle")},onNavigateToLinkedVets={},onNavigateToRegisterAnimal={},
                onNavigateToInviteVets={},onNavigateToVisits={},onNavigateToVisitDetail={},onNavigateToIndicationDetail={},
                profileName=profile.name,livestockContent={
                    CapacityCard(state.capacity)
                    Button(onClick={nav.navigate("cattle")}) { Text(stringResource(R.string.ls_inventory),fontFamily=Poppins) }
                    Text(stringResource(R.string.ls_future),fontFamily=Poppins)
                }) }
            composable("cattle") { pe.novatick.anitec.RancherCattleScreen({}, {},state,vm) }
            composable("profile") { RancherProfileScreen(userName=profile.name+" "+profile.lastName,userEmail=profile.email,onLogoutClick=onLogout) }
            composable("vets") { Column { Header(stringResource(R.string.ls_pending));Text(stringResource(R.string.ls_future),Modifier.padding(24.dp),fontFamily=Poppins) } }
        }
    } }
    }
}
@Composable private fun Header(title: String) {
    Column {
        Box(Modifier.fillMaxWidth().height(55.dp).clip(CattleHeaderShape).background(PrimaryGreenAccent))
        Text(title,Modifier.padding(horizontal=20.dp,vertical=10.dp),fontFamily=Poppins,fontSize=32.sp,
            fontWeight=androidx.compose.ui.text.font.FontWeight.ExtraBold,color=PrimaryGreenAccent)
    }
}
@Composable private fun CapacityCard(value: ReadState<Capacity>) {
    if(value.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    value.value?.let { Text(stringResource(R.string.ls_capacity,it.activeAnimals,it.allowedAnimals,it.remaining),fontFamily=Poppins) }
    if(value.failed) AuthMessage(stringResource(if(value.stale) R.string.ls_stale else R.string.ls_error),true)
}
@Composable private fun Choice(label: String,choices: List<Pair<String,String?>>,onSelect: (String?)->Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box { OutlinedButton(onClick={expanded=true}) { Text(label,fontFamily=Poppins) }
        DropdownMenu(expanded,onDismissRequest={expanded=false}) { choices.forEach { (text,id)->
            DropdownMenuItem(text={Text(text,fontFamily=Poppins)},onClick={expanded=false;onSelect(id)})
        } }
    }
}
@Composable fun LivestockInventoryScreen(state: LivestockState,vm: LivestockViewModel) {
    var farmsOpen by remember { mutableStateOf(false) };var createOpen by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") };var location by remember { mutableStateOf("") }
    LaunchedEffect(state.createdFarm) { if(state.createdFarm!=null) {createOpen=false;farmsOpen=false;name="";location="";vm.clearCreation()} }
    val all=stringResource(R.string.ls_all)
    val statuses=listOf(all to null,stringResource(R.string.ls_active) to "ACTIVO",stringResource(R.string.ls_inactive) to "INACTIVO",
        stringResource(R.string.ls_sold) to "VENDIDO",stringResource(R.string.ls_dead) to "FALLECIDO")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(stringResource(R.string.ls_inventory))
        Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            CapacityCard(state.capacity)
            OutlinedButton(onClick={vm.refreshFoundation();vm.filter(state.filter)}) { Text(stringResource(R.string.ls_refresh),fontFamily=Poppins) }
            TextField(state.filter.search,{ vm.filter(state.filter.copy(search=it),true) },label={Text(stringResource(R.string.ls_search))},
                singleLine=true,modifier=Modifier.fillMaxWidth().testTag("livestock_search"),textStyle=androidx.compose.ui.text.TextStyle(fontFamily=Poppins))
            Choice(state.farms.value?.firstOrNull { it.id==state.filter.farmId }?.name ?: stringResource(if(state.filter.farmId==null) R.string.ls_all_farms else R.string.ls_selected_farm),
                listOf(stringResource(R.string.ls_all_farms) to null)+state.farms.value.orEmpty().map { it.name to it.id },vm::selectFarm)
            TextButton(onClick={farmsOpen=true}) { Text(stringResource(R.string.ls_farms),fontFamily=Poppins) }
            if(state.farms.loading || state.species.loading || state.selectedFarm.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if(state.farms.failed || state.species.failed || state.selectedFarm.failed) AuthMessage(stringResource(R.string.ls_error),true)
            if(state.farms.stale || state.species.stale) Text(stringResource(R.string.ls_stale),fontFamily=Poppins)
            if(state.farms.value?.isEmpty()==true) Text(stringResource(R.string.ls_no_farms),fontFamily=Poppins)
            state.selectedFarm.value?.let { Text(it.location?.takeIf(String::isNotBlank) ?: stringResource(R.string.ls_no_location),fontFamily=Poppins) }
            Choice(state.species.value?.firstOrNull { it.id==state.filter.species }?.name ?: stringResource(R.string.ls_all_species),
                listOf(stringResource(R.string.ls_all_species) to null)+state.species.value.orEmpty().map { it.name to it.id }) { vm.filter(state.filter.copy(species=it)) }
            if(state.species.value?.isEmpty()==true) Text(stringResource(R.string.ls_no_species),fontFamily=Poppins)
            Choice(statuses.first { it.second==state.filter.status?.name }.first,statuses) { vm.filter(state.filter.copy(status=it?.let(AnimalStatus::valueOf))) }
            if(state.inventory.loading) CircularProgressIndicator()
            if(state.inventory.failed) AuthMessage(stringResource(R.string.ls_error),true)
            state.inventory.value?.let { data ->
                Text(stringResource(if(state.filter.farmId==null) R.string.ls_total_global else R.string.ls_total_farm,data.totalActive),fontFamily=Poppins)
                Text(stringResource(R.string.ls_results,data.content.size),fontFamily=Poppins)
                if(data.content.isEmpty()) Text(stringResource(if(state.filter.search.isNotBlank() || state.filter.species!=null || state.filter.status!=AnimalStatus.ACTIVO) R.string.ls_no_results else R.string.ls_no_animals),fontFamily=Poppins)
                data.content.forEach { animal -> Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_cow),null,tint=PrimaryGreen,modifier=Modifier.size(36.dp))
                        Column(Modifier.padding(start=12.dp)) {
                            Text(animal.name ?: animal.code,fontFamily=Poppins)
                            Text(animal.code+" · "+animal.species,fontFamily=Poppins)
                            Text(statuses.first { it.second==animal.status.name }.first,fontFamily=Poppins)
                        }
                    }
                } }
            }
            Text(stringResource(R.string.ls_animals_pending),fontFamily=Poppins,fontSize=12.sp)
        }
    }
    if(farmsOpen) AlertDialog(onDismissRequest={farmsOpen=false},title={Text(stringResource(R.string.ls_farms),fontFamily=Poppins)},
        text={Column(Modifier.verticalScroll(rememberScrollState())) { state.farms.value.orEmpty().forEach { farm ->
            TextButton(onClick={vm.selectFarm(farm.id);farmsOpen=false}) { Column {
                Text(farm.name,fontFamily=Poppins);Text(farm.location?.takeIf(String::isNotBlank) ?: stringResource(R.string.ls_no_location),fontFamily=Poppins)
            } }
        };if(state.farms.value?.isEmpty()==true) Text(stringResource(R.string.ls_no_farms)) }},
        confirmButton={TextButton(onClick={vm.clearCreation();createOpen=true}) { Text(stringResource(R.string.ls_add_farm)) }},
        dismissButton={TextButton(onClick={farmsOpen=false}) { Text(stringResource(R.string.ls_close)) }})
    if(createOpen) AlertDialog(onDismissRequest={if(!state.saving) createOpen=false},title={Text(stringResource(R.string.ls_add_farm),fontFamily=Poppins)},
        text={Column(Modifier.verticalScroll(rememberScrollState())) {
            TextField(name,{name=it},enabled=!state.saving,label={Text(stringResource(R.string.ls_name))},modifier=Modifier.testTag("farm_name"))
            TextField(location,{location=it},enabled=!state.saving,label={Text(stringResource(R.string.ls_location))},modifier=Modifier.testTag("farm_location"))
            state.saveError?.let { AuthMessage(stringResource(if(it=="UNCERTAIN") R.string.ls_uncertain else R.string.ls_form_error),true) }
        }},confirmButton={TextButton(onClick={vm.createFarm(name,location)},enabled=!state.saving && state.saveError!="UNCERTAIN" && name.isNotBlank() && name.length<=150 && location.length<=255) { Text(stringResource(R.string.ls_save)) }},
        dismissButton={TextButton(onClick={createOpen=false},enabled=!state.saving) { Text(stringResource(R.string.ls_cancel)) }})
}
