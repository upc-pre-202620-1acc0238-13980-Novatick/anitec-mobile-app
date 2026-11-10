package pe.novatick.anitec.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.novatick.anitec.R
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.ui.screens.home.RancherMainContainer
import pe.novatick.anitec.ui.theme.AniTecTheme

/** Deliberately absent from release. No AuthGraph or production repository is constructed. */
class LivestockReviewActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {AniTecTheme {
            var scenario by rememberSaveable {mutableStateOf("content")}
            var generation by rememberSaveable {mutableIntStateOf(0)}
            var panel by remember {mutableStateOf(false)}
            val repository=remember(scenario,generation) {DebugLivestockRepository(scenario)}
            val profile=remember {Profile("00000000-0000-0000-0000-000000000001","review@example.invalid","Alex","Rivera",Role.GANADERO)}
            Box(Modifier.fillMaxSize()) {
                key(repository) {RancherMainContainer(livestock=repository,profile=profile,onNavigateToLogin={generation++})}
                TextButton(onClick={panel=true},modifier=Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(end=8.dp).height(32.dp)) {
                    Text(stringResource(R.string.qa_local))
                }
            }
            if(panel) AlertDialog(onDismissRequest={panel=false},title={Text(stringResource(R.string.qa_scenarios))},
                text={Column {
                    Text(stringResource(R.string.qa_notice))
                    val choices=listOf("content" to R.string.qa_content,"empty" to R.string.qa_empty,"no-farms" to R.string.qa_no_farms,
                        "loading" to R.string.qa_loading,"error" to R.string.qa_error,"saving" to R.string.qa_saving,
                        "save-error" to R.string.qa_save_error,"save-uncertain" to R.string.qa_uncertain)
                    choices.forEach { (value,label)->TextButton(onClick={scenario=value;generation++;panel=false}) {Text(stringResource(label))} }
                }},confirmButton={TextButton(onClick={generation++;panel=false}) {Text(stringResource(R.string.qa_restart))}},
                dismissButton={TextButton(onClick={panel=false}) {Text(stringResource(R.string.qa_close))}})
        }}
    }
}
