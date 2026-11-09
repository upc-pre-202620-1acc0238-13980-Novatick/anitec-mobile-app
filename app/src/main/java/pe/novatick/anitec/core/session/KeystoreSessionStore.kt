package pe.novatick.anitec.core.session

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.google.gson.Gson
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** The alias is exclusive to authentication. Version 2 uses one key per login generation. */
class KeystoreSessionStore(context: Context,
    private val deleteRecord: (File) -> Unit = { if(it.exists() && !it.delete()) error("Session deletion failed") },
    private val deleteKey: (KeyStore, String) -> Unit = { ks, alias -> ks.deleteEntry(alias) }
) : SessionStore {
    private val base = File(context.noBackupFilesDir, "auth-session.enc")
    private val file = AtomicFile(base)
    private val residues get() = listOf(base, File(base.path + ".bak"), File(base.path + ".new"))
    private val gson = Gson()
    private val legacy = "anitec.session.v1"
    private val prefix = "anitec.session.v2."
    private val magic = 0x414E4932
    private fun keys() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private fun existing(alias: String): SecretKey? = keys().getKey(alias, null) as? SecretKey
    private fun alias(record: SessionRecord) = record.generation?.let {
        require(java.util.UUID.fromString(it).toString() == it)
        prefix + it
    } ?: legacy
    private fun unpack(bytes: ByteArray): Pair<String, ByteArray> {
        require(bytes.size > 28)
        val input = java.io.DataInputStream(bytes.inputStream())
        if(input.readInt() != magic) return legacy to bytes
        val alias = input.readUTF()
        require(alias == legacy || alias.startsWith(prefix))
        if(alias != legacy) java.util.UUID.fromString(alias.removePrefix(prefix))
        return alias to input.readBytes()
    }
    private fun create(alias: String): SecretKey = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
        init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256).build())
    }.generateKey()
    @Synchronized override fun read(): SessionRecord? {
        if(residues.none { it.exists() }) return null
        val (alias, bytes) = unpack(file.readFully())
        val key = existing(alias) ?: throw java.io.IOException("Session key absent; login required")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, bytes.copyOfRange(0,12)))
        val record = gson.fromJson(String(cipher.doFinal(bytes.copyOfRange(12,bytes.size)), Charsets.UTF_8), SessionRecord::class.java)
        require(alias(record) == alias)
        return record
    }
    @Synchronized override fun write(record: SessionRecord) {
        val alias = alias(record)
        val oldAlias = if(base.exists()) unpack(base.readBytes()).first else null
        val key = existing(alias) ?: run {
            check(oldAlias != alias) { "Closed session key must not be recreated" }
            create(alias)
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encrypted = cipher.iv + cipher.doFinal(gson.toJson(record).toByteArray(Charsets.UTF_8))
        val output = java.io.ByteArrayOutputStream()
        java.io.DataOutputStream(output).use { it.writeInt(magic); it.writeUTF(alias); it.write(encrypted) }
        val stream = file.startWrite()
        try { stream.write(output.toByteArray()); file.finishWrite(stream) }
        catch (e: Exception) { file.failWrite(stream); throw e }
    }
    @Synchronized override fun invalidate(): InvalidationResult {
        // Examine every AtomicFile residue before deletion; errors never suppress another attempt.
        val aliases = mutableSetOf<String>()
        var keyResult = Absence.VERIFIED
        for (residue in residues) if(residue.exists()) try {
            aliases += unpack(residue.readBytes()).first
        } catch (_: Exception) { keyResult = Absence.UNKNOWN }
        if(aliases.isEmpty()) keyResult = Absence.UNKNOWN // No alias: do not claim key absence.
        for (alias in aliases) try {
            val ks = keys()
            deleteKey(ks, alias)
            if(keys().containsAlias(alias)) keyResult = Absence.FAILED
        } catch (_: Exception) { keyResult = Absence.UNKNOWN }
        var filesResult = Absence.VERIFIED
        for (residue in residues) try {
            deleteRecord(residue)
            if(residue.exists()) filesResult = Absence.FAILED
        } catch (_: Exception) { filesResult = Absence.UNKNOWN }
        return InvalidationResult(keyResult, filesResult)
    }
    @Synchronized override fun clear() { check(invalidate().protected) { "Session invalidation unconfirmed" } }
}
