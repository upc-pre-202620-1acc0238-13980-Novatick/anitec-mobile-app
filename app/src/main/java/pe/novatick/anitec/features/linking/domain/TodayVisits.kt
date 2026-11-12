package pe.novatick.anitec.features.linking.domain

import java.text.SimpleDateFormat
import java.util.*

/** Home-only read window: civil day in the device timezone, UTC bounds on the wire. */
data class VisitDay(val start: Long,val end: Long) {
 fun contains(millis: Long)=millis>=start && millis<end
 fun utc(millis: Long)=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.US).apply {timeZone=TimeZone.getTimeZone("UTC")}.format(Date(millis))
 companion object {
  fun current(now: Long=System.currentTimeMillis(),zone: TimeZone=TimeZone.getDefault()):VisitDay {
   val calendar=Calendar.getInstance(zone).apply {timeInMillis=now;set(Calendar.HOUR_OF_DAY,0);set(Calendar.MINUTE,0);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}
   val start=calendar.timeInMillis;calendar.add(Calendar.DAY_OF_MONTH,1);return VisitDay(start,calendar.timeInMillis)
  }
 }
}
data class HomeVisit(val id: String,val animal: String?,val farmer: String,val time: Long,val status: String,val reason: String?)
