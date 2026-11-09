package pe.novatick.anitec.auth

import android.content.ContextWrapper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import pe.novatick.anitec.core.session.*
import java.io.File

/** Requires an Android device: a JVM fake cannot validate AndroidKeyStore. */
@RunWith(AndroidJUnit4::class)
class KeystoreSessionStoreTest {
    private val target=InstrumentationRegistry.getInstrumentation().targetContext
    private val directory=File(target.cacheDir,"isolated-session-store-test")
    private val context=object: ContextWrapper(target) { override fun getNoBackupFilesDir(): File = directory.apply { mkdirs() } }
    @Before fun setup() { KeystoreSessionStore(context).clear() }
    @After fun cleanup() { KeystoreSessionStore(context).clear() }
    @Test fun ciphertextRoundTripAndPendingRotationAreAtomicAcrossInstances() {
        val store=KeystoreSessionStore(context)
        store.write(SessionRecord("11111111-1111-4111-8111-111111111111","test@example.invalid","GANADERO",
            "synthetic-private-access","synthetic-private-refresh",1000,true,"11111111-1111-4111-8111-111111111111",123))
        val bytes=File(directory,"auth-session.enc").readBytes().toString(Charsets.ISO_8859_1)
        assertFalse(bytes.contains("synthetic-private"));assertFalse(bytes.contains("test@example.invalid"))
        val restored=KeystoreSessionStore(context).read()!!
        assertEquals("synthetic-private-refresh",restored.refresh);assertEquals(123L,restored.pendingSince)
        assertEquals("11111111-1111-4111-8111-111111111111",restored.pendingId)
        store.clear();assertNull(KeystoreSessionStore(context).read())
    }
    @Test fun tamperedCiphertextCannotRestoreCredentials() {
        val store=KeystoreSessionStore(context)
        store.write(SessionRecord("id","email","GANADERO","access","refresh",1000,true))
        val file=File(directory,"auth-session.enc");val bytes=file.readBytes()
        bytes[bytes.lastIndex]=(bytes.last().toInt() xor 1).toByte();file.writeBytes(bytes)
        try { store.read();fail("Tampered ciphertext was accepted") } catch(_: javax.crypto.AEADBadTagException) { }
    }
    @Test fun realKeyDeletionMakesResidualUnreadableAndNewLoginUsesAnotherGeneration() {
        val store=KeystoreSessionStore(context, deleteRecord={ if(it.exists()) throw java.io.IOException("Injected deletion failure") })
        val old=SessionRecord("id","email","GANADERO","synthetic-access","synthetic-refresh",1000,true)
        store.write(old)
        val oldAlias="anitec.session.v2." + old.generation
        val ciphertext=File(directory,"auth-session.enc").readBytes()
        val result=store.invalidate()
        assertEquals(Absence.VERIFIED,result.key)
        assertTrue(result.protected)
        assertArrayEquals(ciphertext,File(directory,"auth-session.enc").readBytes())
        val keys=java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        assertFalse(keys.containsAlias(oldAlias))
        try { KeystoreSessionStore(context).read(); fail("Residual was restored") } catch(_: java.io.IOException) { }
        assertFalse(keys.containsAlias(oldAlias))
        val next=SessionRecord("id","email","GANADERO","next-access","next-refresh",2000,true)
        assertNotEquals(old.generation,next.generation)
        KeystoreSessionStore(context).write(next)
        assertEquals(next.generation,KeystoreSessionStore(context).read()!!.generation)
        assertFalse(keys.containsAlias(oldAlias))
    }
    @Test fun keyFailureDoesNotSuppressFileDeletion() {
        val store=KeystoreSessionStore(context, deleteKey={ _,_ -> throw java.io.IOException("Injected key failure") })
        val record=SessionRecord("id","email","GANADERO","access","refresh",1000,true)
        store.write(record)
        try {
            val result=store.invalidate()
            assertEquals(Absence.UNKNOWN,result.key)
            assertEquals(Absence.VERIFIED,result.files)
            assertTrue(result.protected)
            assertNull(KeystoreSessionStore(context).read())
        } finally {
            java.security.KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .deleteEntry("anitec.session.v2." + record.generation)
        }
    }
    @Test fun repositoryGuardSurvivesContainerRecreationInSameProcess() {
        assertSame(pe.novatick.anitec.core.di.AuthContainer(target).repository,
            pe.novatick.anitec.core.di.AuthContainer(target).repository)
    }
}
