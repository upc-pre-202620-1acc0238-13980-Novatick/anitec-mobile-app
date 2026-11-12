package pe.novatick.anitec.features.livestock.infrastructure
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import java.util.UUID

internal fun recentCare(items:List<CareSummaryDto>?):List<CareSummary>?=items?.map{d->
 fun text(v:String?)=v?.takeIf(String::isNotBlank) ?: throw AuthFailure(AuthProblem.INVALID_RESPONSE)
 val id=text(d.id);if(!runCatching{UUID.fromString(id).toString()==id.lowercase()}.getOrDefault(false) || !validCareDate(d.date.orEmpty()))throw AuthFailure(AuthProblem.INVALID_RESPONSE)
 CareSummary(id,text(d.title),text(d.vetName),d.date!!,d.summary.orEmpty())
}?.also{if(it.map{d->d.id}.distinct().size!=it.size)throw AuthFailure(AuthProblem.INVALID_RESPONSE)}?.sortedByDescending{it.date}
