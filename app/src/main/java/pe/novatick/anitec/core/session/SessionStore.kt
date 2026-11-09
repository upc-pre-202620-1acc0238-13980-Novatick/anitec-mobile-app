package pe.novatick.anitec.core.session

import androidx.annotation.Keep

// Never expose generated data-class toString() containing credentials.
@Keep
class SessionRecord(val accountId: String, val email: String, val role: String, val access: String,
                    val refresh: String, val expiresAt: Long, val persistent: Boolean,
                    val pendingId: String? = null, val pendingSince: Long? = null,
                    val generation: String? = java.util.UUID.randomUUID().toString()) {
    override fun toString() = "SessionRecord(REDACTED)"
    fun pending(id: String, now: Long) = SessionRecord(accountId,email,role,access,refresh,expiresAt,persistent,id,now,generation)
}
enum class Absence { VERIFIED, FAILED, UNKNOWN }
data class InvalidationResult(val key: Absence, val files: Absence) {
    val protected: Boolean get() = key == Absence.VERIFIED || files == Absence.VERIFIED
}
interface SessionStore {
    fun read(): SessionRecord?
    fun write(record: SessionRecord)
    fun invalidate(): InvalidationResult = try {
        clear(); InvalidationResult(Absence.UNKNOWN, Absence.VERIFIED)
    } catch (_: Exception) { InvalidationResult(Absence.UNKNOWN, Absence.FAILED) }
    fun clear()
}
