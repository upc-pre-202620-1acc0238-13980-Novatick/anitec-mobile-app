package pe.novatick.anitec.core.environment

import android.app.Activity
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import pe.novatick.anitec.R

/** Alpha source set only: shared staging credential is never written to disk. */
internal object VariantAccess {
    fun isReady()=MemoryStagingKey.read()!=null
    fun configure(builder: OkHttpClient.Builder)=builder.addInterceptor(StagingHeader.interceptor)
        .followRedirects(false).followSslRedirects(false)

    private fun probe(value: String): Boolean {
        val client=OkHttpClient.Builder().retryOnConnectionFailure(false).followRedirects(false)
            .followSslRedirects(false).callTimeout(15,TimeUnit.SECONDS).build()
        val request=StagingHeader.prepare(Request.Builder()
            .url("https://anitec-staging.onrender.com/api/v1/account/me").get().build(),value)
        return client.newCall(request).execute().use {it.code==401}
    }

    @Composable fun Gate(refresh: suspend ()->Unit,content: @Composable ()->Unit) {
        val ready by MemoryStagingKey.ready.collectAsState()
        var edit by remember {mutableStateOf(false)}
        val context=LocalContext.current
        val window=remember(context) {generateSequence(context) {(it as? ContextWrapper)?.baseContext}
            .filterIsInstance<Activity>().first().window}
        DisposableEffect(window) {
            val existed=window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE!=0
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            onDispose {if(!existed) window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)}
        }
        if(ready && !edit) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal=12.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.staging_internal_label),Modifier.weight(1f).padding(top=12.dp))
                    TextButton(onClick={edit=true}) {Text(stringResource(R.string.staging_access))}
                }
                Box(Modifier.weight(1f)) {content()}
            }
        } else {
            var draft by remember {mutableStateOf("")}
            var busy by remember {mutableStateOf(false)}
            var failure by remember {mutableStateOf(false)}
            var sessionUpdated by remember {mutableStateOf(false)}
            var sessionFailure by remember {mutableStateOf(false)}
            val scope=rememberCoroutineScope()
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.staging_setup_title),style=MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.staging_setup_info))
                OutlinedTextField(draft,{draft=it;failure=false},enabled=!busy,singleLine=true,
                    label={Text(stringResource(R.string.staging_key_label))},visualTransformation=PasswordVisualTransformation(),
                    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,autoCorrectEnabled=false),modifier=Modifier.fillMaxWidth())
                if(failure) Text(stringResource(R.string.staging_setup_failed),color=MaterialTheme.colorScheme.error)
                if(busy) CircularProgressIndicator()
                Button(onClick={
                    busy=true;failure=false
                    val submitted=draft
                    scope.launch {
                        try {
                            val accepted=withContext(Dispatchers.IO) {probe(submitted)}
                            if(accepted) {MemoryStagingKey.set(submitted);draft="";edit=false} else failure=true
                        } catch(e: CancellationException) {throw e}
                          catch(_: Exception) {failure=true}
                        finally {busy=false}
                    }
                },enabled=!busy && MemoryStagingKey.valid(draft)) {Text(stringResource(R.string.staging_validate))}
                if(ready) {
                    if(sessionUpdated) Text(stringResource(R.string.staging_session_updated))
                    if(sessionFailure) Text(stringResource(R.string.staging_session_failed),color=MaterialTheme.colorScheme.error)
                    TextButton(onClick={
                        busy=true;sessionUpdated=false;sessionFailure=false
                        scope.launch {
                            try {refresh();sessionUpdated=true}
                            catch(e: CancellationException) {throw e}
                            catch(_: Exception) {sessionFailure=true}
                            finally {busy=false}
                        }
                    },enabled=!busy) {Text(stringResource(R.string.staging_refresh_session))}
                    TextButton(onClick={draft="";edit=false},enabled=!busy) {Text(stringResource(R.string.staging_cancel))}
                    TextButton(onClick={draft="";MemoryStagingKey.clear();edit=false},enabled=!busy) {Text(stringResource(R.string.staging_remove))}
                }
            }
        }
    }
}
