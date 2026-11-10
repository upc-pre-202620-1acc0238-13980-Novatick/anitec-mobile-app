package pe.novatick.anitec.features.livestock.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.theme.*

/** Native Compose adaptation of Figma 104:2266 / 104:4327; no business rules. */
internal object LivestockVisuals {
    val Ink=Color(0xFF24342D)
    val Secondary=Color(0xFF66766F)
    val Pale=Color(0xFFF1F5F9)
    val Border=Color(0xFFE5ECE8)
    val Mint=Color(0xFFE8F9EE)
}
@Composable internal fun LsText(value: String,modifier: Modifier=Modifier,size: Int=13,bold: Boolean=false,color: Color=LivestockVisuals.Ink) {
    Text(value,modifier,fontFamily=Poppins,fontSize=size.sp,lineHeight=(size+5).sp,
        fontWeight=if(bold) FontWeight.SemiBold else FontWeight.Normal,color=color)
}
@Composable internal fun LsAction(label: String,onClick: ()->Unit,modifier: Modifier=Modifier,primary: Boolean=false,enabled: Boolean=true,icon: Int?=null) {
    if(primary) Button(onClick,modifier.heightIn(min=48.dp),enabled=enabled,shape=RoundedCornerShape(14.dp),
        colors=ButtonDefaults.buttonColors(containerColor=PrimaryGreenAccent,contentColor=Color.White,
            disabledContainerColor=PrimaryGreenAccent.copy(alpha=0.22f),disabledContentColor=PrimaryGreen)) {
        if(icon!=null) {Icon(painterResource(icon),null,modifier=Modifier.size(20.dp));Spacer(Modifier.width(8.dp))}
        LsText(label,bold=true,color=if(enabled) Color.White else PrimaryGreen)
    } else OutlinedButton(onClick,modifier.heightIn(min=48.dp),enabled=enabled,shape=RoundedCornerShape(14.dp),
        border=BorderStroke(1.dp,PrimaryGreenAccent),colors=ButtonDefaults.outlinedButtonColors(contentColor=PrimaryGreen)) {
        LsText(label,bold=true,color=PrimaryGreen)
    }
}
@Composable internal fun LsNotice(text: String,error: Boolean=false) {
    val color=if(error) Color(0xFFAE4141) else LivestockVisuals.Secondary
    Row(Modifier.fillMaxWidth().background(if(error) Color(0xFFFFF0F0) else LivestockVisuals.Pale,RoundedCornerShape(12.dp)).padding(12.dp)) {
        LsText(text,size=12,color=color)
    }
}
@Composable internal fun LsLoading(label: String) {
    Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)) {
        CircularProgressIndicator(Modifier.size(20.dp),color=PrimaryGreen,strokeWidth=2.dp)
        LsText(label,size=12,color=LivestockVisuals.Secondary)
    }
}
@Composable internal fun LsReadIssue(label: String,stale: Boolean,onRetry: ()->Unit) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            LsText(label,size=11,color=LivestockVisuals.Secondary)
            if(stale) LsText(stringResource(R.string.ls_stale),size=10,color=LivestockVisuals.Secondary)
        }
        TextButton(onClick=onRetry) {LsText(stringResource(R.string.ls_retry),size=11,bold=true,color=PrimaryGreen)}
    }
}
@Composable internal fun LsEmpty(title: String,detail: String) {
    Column(Modifier.fillMaxWidth().padding(vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(64.dp).background(LivestockVisuals.Mint,RoundedCornerShape(20.dp)),contentAlignment=Alignment.Center) {
            Icon(painterResource(R.drawable.ic_cow),null,tint=PrimaryGreen,modifier=Modifier.size(36.dp))
        }
        Text(title,fontFamily=Poppins,fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=LivestockVisuals.Ink,textAlign=TextAlign.Center)
        Text(detail,fontFamily=Poppins,fontSize=12.sp,lineHeight=18.sp,color=LivestockVisuals.Secondary,textAlign=TextAlign.Center)
    }
}
@Composable internal fun LsSearch(value: String,onChange: (String)->Unit) {
    BasicTextField(value,onChange,singleLine=true,textStyle=TextStyle(fontFamily=Poppins,fontSize=13.sp,color=LivestockVisuals.Ink),
        keyboardOptions=KeyboardOptions(imeAction=ImeAction.Search),cursorBrush=SolidColor(PrimaryGreen),
        modifier=Modifier.fillMaxWidth().testTag("livestock_search"),decorationBox={ editor ->
            Row(Modifier.fillMaxWidth().heightIn(min=46.dp).background(LivestockVisuals.Pale,RoundedCornerShape(14.dp)).padding(horizontal=14.dp,vertical=12.dp),
                verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                Icon(painterResource(R.drawable.ic_search),stringResource(R.string.ls_search),tint=NeutralSlate,modifier=Modifier.size(20.dp))
                Box(Modifier.weight(1f)) { if(value.isEmpty()) LsText(stringResource(R.string.ls_search),color=NeutralSlate);editor() }
            }
        })
}
@Composable internal fun LsField(value: String,onChange: (String)->Unit,label: String,help: String,tag: String,error: String?,enabled: Boolean=true) {
    Column(verticalArrangement=Arrangement.spacedBy(5.dp)) {
        LsText(label,bold=true)
        OutlinedTextField(value,onChange,modifier=Modifier.fillMaxWidth().testTag(tag),singleLine=true,enabled=enabled,
            textStyle=TextStyle(fontFamily=Poppins,fontSize=13.sp),shape=RoundedCornerShape(14.dp),isError=error!=null,
            colors=OutlinedTextFieldDefaults.colors(unfocusedContainerColor=LivestockVisuals.Pale,focusedContainerColor=Color.White,
                unfocusedBorderColor=LivestockVisuals.Border,focusedBorderColor=PrimaryGreen),
            supportingText={LsText(error ?: help,size=11,color=if(error!=null) Color(0xFFAE4141) else LivestockVisuals.Secondary)})
    }
}
@Composable internal fun LsModal(title: String,onDismiss: ()->Unit,content: @Composable ColumnScope.()->Unit,actions: @Composable RowScope.()->Unit) {
    Dialog(onDismiss,DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
        Box(Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(22.dp),contentAlignment=Alignment.Center) {
            Surface(Modifier.widthIn(max=400.dp).fillMaxWidth().heightIn(max=640.dp).testTag("livestock_modal_surface"),shape=RoundedCornerShape(20.dp),color=Color.White,tonalElevation=0.dp) {
                Column(Modifier.padding(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f,fill=false).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                        Text(title,Modifier.fillMaxWidth(),fontFamily=Poppins,fontWeight=FontWeight.Bold,fontSize=15.sp,lineHeight=20.sp,color=PrimaryGreen,textAlign=TextAlign.Center)
                        content()
                    }
                    Row(Modifier.fillMaxWidth().testTag("livestock_modal_actions"),horizontalArrangement=Arrangement.spacedBy(9.dp),content=actions)
                }
            }
        }
    }
}
