package pe.novatick.anitec.features.livestock.presentation
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.theme.*

@Composable private fun ObservationCard(item: AnimalObservation) {
    Surface(Modifier.fillMaxWidth().testTag("observation_"+item.id),shape=RoundedCornerShape(15.dp),color=Color.White,shadowElevation=3.dp) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(PrimaryGreenAccent))
            Column(Modifier.weight(1f).padding(horizontal=16.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                LsText(item.text,size=11)
                val metadata=listOfNotNull(item.authorName,item.date).joinToString(" \u00B7 ")
                if(metadata.isNotBlank()) LsText(metadata,size=9,color=LivestockVisuals.Secondary)
            }
        }
    }
}
@Composable internal fun ObservationSection(id: String,state: ObservationState,vm: AnimalObservationViewModel,all: ()->Unit,add: ()->Unit) {
    val current=if(state.animalId==id) state.items else ReadState(loading=true)
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
        LsText(stringResource(R.string.obs_title),Modifier.weight(1f),bold=true)
        TextButton(onClick=all,enabled=!state.busy) {LsText(stringResource(R.string.obs_all,current.value?.size ?: 0),size=10,color=PrimaryGreen)}
    }
    LsText(stringResource(R.string.obs_hint),size=10,color=LivestockVisuals.Secondary)
    ObservationContent(current,{vm.load(id)},recent=true)
    ObservationAddButton(add,!state.busy)
}
@Composable private fun ObservationContent(value: ReadState<List<AnimalObservation>>,retry: ()->Unit,recent: Boolean=false) {
    if(value.loading) LsLoading(stringResource(R.string.ls_loading))
    if(value.failed) {LsNotice(stringResource(R.string.obs_read_error),true);LsAction(stringResource(R.string.ls_retry),retry)}
    value.value?.let {items ->
        if(items.isEmpty()) LsNotice(stringResource(R.string.obs_empty))
        // API returns the complete descending history. No client-invented server pagination.
        (if(recent) items.take(1) else items).forEach {ObservationCard(it)}
    }
}
@Composable private fun ObservationAddButton(onClick: ()->Unit,enabled: Boolean=true) {
    OutlinedButton(onClick,Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=enabled,shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,PrimaryGreen)) {
        Icon(painterResource(R.drawable.ls_plus_official),null,Modifier.size(20.dp),tint=PrimaryGreen)
        Spacer(Modifier.width(8.dp));LsText(stringResource(R.string.obs_add),size=12,bold=true,color=PrimaryGreen)
    }
}
@Composable fun ObservationListScreen(id: String,state: ObservationState,vm: AnimalObservationViewModel,back: ()->Unit,add: ()->Unit) {
    AnimalPage(stringResource(R.string.obs_title),back,centered=true) {
        ObservationContent(if(state.animalId==id) state.items else ReadState(loading=true),{vm.load(id)})
        ObservationAddButton(add,!state.busy)
    }
}
@Composable fun ObservationForm(animal: AnimalDetail?,state: ObservationState,vm: AnimalObservationViewModel,back: ()->Unit,confirmed: ()->Unit) {
    if(animal==null) {AnimalPage(stringResource(R.string.obs_add),back,centered=true) {LsNotice(stringResource(R.string.animal_missing),true)};return}
    var text by remember(animal.id) {mutableStateOf("")};var attempted by remember(animal.id) {mutableStateOf(false)}
    val error=state.errors[animal.id]
    LaunchedEffect(state.confirmedFor) {if(state.confirmedFor==animal.id) {vm.begin();confirmed()}}
    BackHandler(state.busy) {}
    AnimalPage(stringResource(R.string.obs_add),back,busy=state.busy,centered=true) {
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
            Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(15.dp)) {
                AnimalSpeciesImage(animal.species,animal.photoUrl)
                Column(Modifier.weight(1f)) {LsText(animal.name?.takeIf(String::isNotBlank) ?: animal.code,size=18,bold=true,color=PrimaryGreen);LsText(animal.code+" \u00B7 "+animal.species,size=12,color=LivestockVisuals.Secondary)}
            }
        }
        Row(Modifier.fillMaxWidth().background(Color(0xFFF4FCF7),RoundedCornerShape(13.dp)).padding(11.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            Icon(painterResource(R.drawable.ls_document_official),null,Modifier.size(20.dp),tint=PrimaryGreen)
            LsText(stringResource(R.string.obs_info),size=10,color=LivestockVisuals.Secondary)
        }
        LsText(stringResource(R.string.obs_label),size=12)
        val label=stringResource(R.string.obs_label)
        BasicTextField(text,{text=it},enabled=!state.busy,textStyle=TextStyle(fontFamily=Poppins,fontSize=12.sp,color=LivestockVisuals.Ink),cursorBrush=SolidColor(PrimaryGreen),
            modifier=Modifier.fillMaxWidth().heightIn(min=90.dp).background(LivestockVisuals.Pale,RoundedCornerShape(13.dp)).padding(13.dp).testTag("observation_text").semantics {contentDescription=label})
        if(attempted && !validObservationText(text)) LsText(stringResource(R.string.obs_invalid),size=11,color=Color(0xFFAE4141))
        if(error!=null) LsNotice(when {
            error.code=="UNCERTAIN"->stringResource(R.string.obs_uncertain)
            error.status==403->stringResource(R.string.animal_forbidden)
            error.status==404->stringResource(R.string.animal_missing)
            error.status==400->stringResource(R.string.obs_invalid)
            else->stringResource(R.string.obs_save_error)
        },true)
        if(error?.code=="UNCERTAIN") {
            LsAction(stringResource(R.string.obs_check),{vm.load(animal.id)})
            state.items.value?.forEach {ObservationCard(it)}
            if(state.items.loading) LsLoading(stringResource(R.string.ls_loading))
            if(state.items.failed) LsNotice(stringResource(R.string.obs_read_error),true)
            LsAction(stringResource(R.string.obs_allow),{vm.acknowledge(animal.id)},enabled=state.animalId==animal.id && state.items.value!=null && !state.items.failed && !state.items.loading)
        }
        if(state.busy) LsLoading(stringResource(R.string.obs_saving))
        LsAction(stringResource(R.string.obs_save),{attempted=true;if(validObservationText(text)) vm.save(animal.id,text)},Modifier.fillMaxWidth().testTag("observation_save"),primary=true,enabled=!state.busy && error?.code!="UNCERTAIN")
        TextButton(onClick=back,enabled=!state.busy,modifier=Modifier.align(Alignment.CenterHorizontally)) {LsText(stringResource(R.string.ls_cancel),size=11,color=LivestockVisuals.Secondary)}
    }
}
