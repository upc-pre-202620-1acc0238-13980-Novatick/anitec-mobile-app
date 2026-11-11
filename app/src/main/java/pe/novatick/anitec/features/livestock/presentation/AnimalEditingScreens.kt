package pe.novatick.anitec.features.livestock.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.AuthFailure
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.theme.*

@Composable internal fun animalMutationFailureText(error: AuthFailure): String=when(error.code) {
    "ANIMAL_VERSION_CONFLICT"->stringResource(R.string.animal_conflict)
    "UNCERTAIN"->stringResource(R.string.animal_mutation_uncertain)
    else->animalFailureText(error)
}

/** Figma 104:3417. Contract-only editing: farm/photo are preserved; no upload/generator API invented. */
@Composable fun AnimalEditScreen(state: LivestockState,vm: LivestockViewModel,back: ()->Unit,saved: ()->Unit) {
    val animal=state.animalDetail.value
    LaunchedEffect(state.mutation.completed) {if(state.mutation.completed=="EDIT") saved()}
    if(animal==null) {
        AnimalPage(stringResource(R.string.animal_edit),back) {
            if(state.animalDetail.loading) LsLoading(stringResource(R.string.ls_loading))
            state.animalDetailError?.let {LsNotice(animalFailureText(it),true);LsAction(stringResource(R.string.ls_retry),{state.animalDetailId?.let(vm::reloadMutation)})}
        };return
    }
    key(animal.id,animal.version) {
        var draft by remember {mutableStateOf(AnimalEdit.from(animal))}
        var attempted by remember {mutableStateOf(false)}
        val errors=if(attempted) animalEditErrors(animal,draft) else emptySet()
        val busy=state.mutation.busy
        val blocked=state.mutation.error?.code in listOf("UNCERTAIN","ANIMAL_VERSION_CONFLICT")
        AnimalPage(stringResource(R.string.animal_edit),back,busy) {
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(15.dp),border=BorderStroke(1.dp,LivestockVisuals.Border),color=Color.White) {
                Row(Modifier.heightIn(min=116.dp).padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    AnimalSpeciesImage(animal.species,animal.photoUrl,83)
                    val photos=listOf(R.string.animal_take_photo to R.drawable.animal_edit_camera_official,R.string.animal_upload_photo to R.drawable.animal_edit_upload_official)
                    photos.forEach {(label,icon)->
                        Button(onClick={},enabled=false,modifier=Modifier.weight(1f).heightIn(min=40.dp),shape=RoundedCornerShape(10.dp),contentPadding=PaddingValues(5.dp),
                            colors=ButtonDefaults.buttonColors(disabledContainerColor=PrimaryGreenAccent.copy(alpha=.45f),disabledContentColor=Color.White)) {
                            Icon(painterResource(icon),null,Modifier.size(if(label==R.string.animal_take_photo) 16.dp else 13.dp,if(label==R.string.animal_take_photo) 16.dp else 10.dp),tint=Color.Unspecified)
                            Spacer(Modifier.width(3.dp));Text(stringResource(label),fontFamily=Poppins,fontSize=10.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp),verticalAlignment=Alignment.Bottom) {
                Box(Modifier.weight(1f)) {AnimalField(draft.code,{draft=draft.copy(code=it)},stringResource(R.string.animal_edit_code),"","animal_edit_code",
                    if("code" in errors) stringResource(R.string.animal_invalid) else null,!busy)}
                Button(onClick={},enabled=false,modifier=Modifier.width(115.dp).height(48.dp),shape=RoundedCornerShape(10.dp),contentPadding=PaddingValues(8.dp),
                    colors=ButtonDefaults.buttonColors(disabledContainerColor=PrimaryGreenAccent.copy(alpha=.45f),disabledContentColor=Color.White)) {
                    Text(stringResource(R.string.animal_generate),fontFamily=Poppins,fontWeight=FontWeight.Bold,fontSize=11.sp)
                }
            }
            val sexes=listOf(AnimalSex.MACHO to stringResource(R.string.animal_male),AnimalSex.HEMBRA to stringResource(R.string.animal_female),AnimalSex.DESCONOCIDO to stringResource(R.string.animal_unspecified))
            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_species),state.species.value?.firstOrNull {it.id==draft.speciesId}?.name ?: animal.species,
                    state.species.value.orEmpty().map {it.id to it.name},!busy,"animal_edit_species") {draft=draft.copy(speciesId=it)}}
                Box(Modifier.weight(1f)) {AnimalSelect(stringResource(R.string.animal_sex),sexes.first {it.first==draft.sex}.second,sexes.map {it.first.name to it.second},!busy,"animal_edit_sex") {draft=draft.copy(sex=AnimalSex.valueOf(it))}}
            }
            if(state.species.failed) {LsNotice(stringResource(R.string.ls_partial_error),true);LsAction(stringResource(R.string.ls_retry),vm::refreshFoundation)}
            if("speciesId" in errors) LsText(stringResource(R.string.animal_invalid),size=11,color=Color.Red)
            AnimalField(draft.name.orEmpty(),{draft=draft.copy(name=it.takeIf(String::isNotEmpty))},stringResource(R.string.animal_name),"","animal_edit_name",if("name" in errors) stringResource(R.string.animal_invalid) else null,!busy)
            AnimalField(draft.breed.orEmpty(),{draft=draft.copy(breed=it.takeIf(String::isNotEmpty))},stringResource(R.string.animal_breed),"","animal_edit_breed",if("breed" in errors) stringResource(R.string.animal_invalid) else null,!busy)
            AnimalBirthField(draft.birthDate.orEmpty(),{draft=draft.copy(birthDate=it.takeIf(String::isNotEmpty))},if("birthDate" in errors) stringResource(R.string.animal_invalid) else null,!busy)
            Surface(color=Color(0xFFF4FCF7),shape=RoundedCornerShape(13.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal=11.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    Icon(painterResource(R.drawable.animal_edit_info_official),null,Modifier.size(20.dp),tint=Color.Unspecified)
                    LsText(stringResource(R.string.animal_code_unique),Modifier.weight(1f),size=10,color=LivestockVisuals.Secondary)
                }
            }
            state.mutation.error?.let {LsNotice(animalMutationFailureText(it),true)}
            if(blocked) LsAction(stringResource(R.string.animal_reload),{vm.reloadMutation(animal.id)},Modifier.testTag("animal_reload"))
            if(busy) LsLoading(stringResource(R.string.ls_saving))
            LsAction(stringResource(R.string.animal_save_changes),{attempted=true;vm.updateAnimal(animal,draft)},Modifier.fillMaxWidth().testTag("animal_edit_submit"),primary=true,enabled=!busy && !blocked)
            LsText(stringResource(R.string.animal_photo),size=10,color=LivestockVisuals.Secondary)
            LsText(stringResource(R.string.animal_generation_unavailable),size=10,color=LivestockVisuals.Secondary)
            LsText(stringResource(R.string.animal_preserved_farm,animal.farmName),size=10,color=LivestockVisuals.Secondary)
        }
    }
}

/** Official Figma 104:4327: 44% scrim, white 332dp card, destructive-left / cancel-right. */
@Composable internal fun AnimalDeactivationDialog(animal: AnimalDetail,mutation: AnimalMutation,cancel: ()->Unit,confirm: ()->Unit) {
    Dialog(onDismissRequest={if(!mutation.busy) cancel()},properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
        val view=androidx.compose.ui.platform.LocalView.current
        DisposableEffect(view) {
            val window=(view.parent as? DialogWindowProvider)?.window
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setDimAmount(.44f)
            onDispose {}
        }
        Box(Modifier.fillMaxSize().systemBarsPadding().padding(22.dp),contentAlignment=Alignment.Center) {
            Surface(Modifier.widthIn(max=332.dp).fillMaxWidth().testTag("animal_deactivation_dialog"),shape=RoundedCornerShape(20.dp),color=Color.White) {
                Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.animal_deactivate_title,animal.name?.takeIf(String::isNotBlank) ?: animal.code),Modifier.fillMaxWidth(),fontFamily=Poppins,fontWeight=FontWeight.Bold,fontSize=15.sp,lineHeight=19.5.sp,color=PrimaryGreen,textAlign=TextAlign.Center)
                    Text(stringResource(R.string.animal_deactivate_explanation),Modifier.fillMaxWidth(),fontFamily=Poppins,fontSize=10.sp,lineHeight=16.5.sp,color=LivestockVisuals.Secondary,textAlign=TextAlign.Center)
                    mutation.error?.let {LsNotice(animalMutationFailureText(it),true)}
                    if(mutation.busy) LsLoading(stringResource(R.string.ls_saving))
                    Row(Modifier.fillMaxWidth().padding(top=8.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                        Button(confirm,Modifier.weight(1f).heightIn(min=42.dp).testTag("animal_confirm_deactivate"),enabled=!mutation.busy && mutation.error?.code !in listOf("UNCERTAIN","ANIMAL_VERSION_CONFLICT"),shape=RoundedCornerShape(14.dp),contentPadding=PaddingValues(8.dp),
                            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFF0F0),contentColor=Color(0xFFDF6262))) {
                            Text(stringResource(R.string.animal_deactivate),fontFamily=Poppins,fontWeight=FontWeight.Bold,fontSize=12.sp)
                        }
                        OutlinedButton(cancel,Modifier.weight(1f).heightIn(min=42.dp).testTag("animal_cancel_deactivate"),enabled=!mutation.busy,shape=RoundedCornerShape(14.dp),border=BorderStroke(1.dp,PrimaryGreenAccent),contentPadding=PaddingValues(8.dp)) {
                            Text(stringResource(R.string.ls_cancel),fontFamily=Poppins,fontWeight=FontWeight.Bold,fontSize=12.sp,color=PrimaryGreen)
                        }
                    }
                }
            }
        }
    }
}
