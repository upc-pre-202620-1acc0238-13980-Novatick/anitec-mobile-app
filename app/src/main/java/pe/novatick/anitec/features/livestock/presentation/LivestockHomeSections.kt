package pe.novatick.anitec.features.livestock.presentation

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.novatick.anitec.R
import pe.novatick.anitec.ui.theme.*

@Composable internal fun rememberLivestockUnavailableAction(): ()->Unit {
    val context=LocalContext.current
    val message=stringResource(R.string.ls_available_later)
    return remember(context,message) {{Toast.makeText(context,message,Toast.LENGTH_SHORT).show()}}
}

/** Clinical data is not integrated in 3A. These neutral placeholders make no empty-list claim. */
@Composable internal fun LivestockHomeSections() {
    val unavailable=rememberLivestockUnavailableAction()
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        LsText(stringResource(R.string.ls_quick_actions),bold=true)
        Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            HomeQuickAction(stringResource(R.string.ls_register_short),R.drawable.ls_plus_official,Modifier.weight(1f),unavailable)
            HomeQuickAction(stringResource(R.string.ls_invite_vet),R.drawable.ls_mail_official,Modifier.weight(1f),unavailable)
        }
    }
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            LsText(stringResource(R.string.ls_upcoming_visits),bold=true)
            TextButton(onClick=unavailable) {LsText(stringResource(R.string.ls_view_visits),size=11,bold=true,color=PrimaryGreen)}
        }
        ClinicalPlaceholder(R.drawable.ic_calendar,stringResource(R.string.ls_visits_neutral))
    }
    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
        LsText(stringResource(R.string.ls_recent_indications),bold=true)
        ClinicalPlaceholder(R.drawable.ls_document_official,stringResource(R.string.ls_indications_neutral))
    }
}

@Composable private fun HomeQuickAction(label: String,icon: Int,modifier: Modifier,onClick: ()->Unit) {
    Surface(modifier.heightIn(min=64.dp).clickable(onClick=onClick),shape=RoundedCornerShape(15.dp),color=Color(0xFFF4FCF7),border=BorderStroke(1.dp,LivestockVisuals.Mint)) {
        Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(33.dp),shape=RoundedCornerShape(11.dp),color=PrimaryGreenAccent) {
                Box(contentAlignment=Alignment.Center) {Icon(painterResource(icon),null,tint=Color.Unspecified,modifier=Modifier.size(20.dp))}
            }
            LsText(label,Modifier.weight(1f),size=11,bold=true)
        }
    }
}

@Composable private fun ClinicalPlaceholder(icon: Int,label: String) {
    Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Icon(painterResource(icon),null,tint=NeutralSlate,modifier=Modifier.size(28.dp))
        Text(label,fontFamily=Poppins,fontSize=12.sp,color=NeutralSlate,textAlign=TextAlign.Center)
    }
}
