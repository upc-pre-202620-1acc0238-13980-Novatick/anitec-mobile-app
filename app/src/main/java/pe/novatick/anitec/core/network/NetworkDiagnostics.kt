package pe.novatick.anitec.core.network

import android.util.Log
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

/** Internal builds only. No URLs, query strings, identifiers, headers or response bodies. */
internal object NetworkDiagnostics {
    private val sequence=AtomicLong()
    val factory=EventListener.Factory { call ->
        val id=sequence.incrementAndGet()
        val start=System.nanoTime()
        val path=call.request().url.encodedPath
        val category=when {
            path.endsWith("/observations")->"observations"
            path.endsWith("/refresh-token")->"refresh"
            path.endsWith("/account/me")->"identity"
            path.endsWith("/animals")->"inventory"
            path.endsWith("/capacity")->"capacity"
            path.matches(Regex(".*/animals/[^/]+"))->"animal"
            path.contains("/farms")->"farms"
            else->"other"
        }
        object: EventListener() {
            private fun record(event: String) {
                Log.i("AnitecNet","id=$id operation=$category method=${call.request().method} ms=${(System.nanoTime()-start)/1_000_000} $event")
            }
            override fun callStart(call: Call)=record("start")
            override fun responseHeadersEnd(call: Call,response: Response)=record("http=${response.code}")
            override fun callEnd(call: Call)=record("complete")
            override fun responseFailed(call: Call,ioe: IOException)=record("attemptFailure=${ioe.javaClass.simpleName} eof=${ioe.message?.contains("unexpected end")==true}")
            override fun callFailed(call: Call,ioe: IOException) {
                val flags=listOf("unexpected end","reset","closed","timeout","CLEARTEXT","Canceled").filter {ioe.message?.contains(it,ignoreCase=true)==true}
                record("failure=${ioe.javaClass.simpleName} canceled=${call.isCanceled()} flags=$flags")
            }
        }
    }
}
