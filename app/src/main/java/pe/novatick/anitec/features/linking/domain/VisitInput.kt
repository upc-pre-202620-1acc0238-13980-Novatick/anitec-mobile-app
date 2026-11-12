package pe.novatick.anitec.features.linking.domain
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.*

/** Civil input is interpreted in the selected device zone; nonexistent DST times fail roundtrip. */
fun visitLocalMillis(date:String,time:String,zone:TimeZone=TimeZone.getDefault()):Long? {
 if(!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || !time.matches(Regex("\\d{2}:\\d{2}")))return null
 val input="$date $time";val parser=SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).apply{isLenient=false;timeZone=zone}
 val pos=ParsePosition(0);val parsed=parser.parse(input,pos) ?: return null
 return parsed.time.takeIf{pos.index==input.length && parser.format(parsed)==input}
}
fun visitInputValid(owner:String?,animal:String?,reason:String,date:String,time:String,now:Long=System.currentTimeMillis(),zone:TimeZone=TimeZone.getDefault())=
 !owner.isNullOrBlank() && !animal.isNullOrBlank() && reason.isNotBlank() && reason.length<=255 && (visitLocalMillis(date,time,zone) ?: Long.MIN_VALUE)>now
