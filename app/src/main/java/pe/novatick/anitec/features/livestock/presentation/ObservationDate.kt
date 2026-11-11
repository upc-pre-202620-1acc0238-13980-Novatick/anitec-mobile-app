package pe.novatick.anitec.features.livestock.presentation
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.Locale
import java.util.TimeZone

/** Format a civil date in UTC at both ends; never reinterpret it in the device timezone. */
internal fun formatObservationDate(value: String,locale: Locale): String {
    val utc=TimeZone.getTimeZone("UTC")
    val parser=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply {isLenient=false;timeZone=utc}
    val position=ParsePosition(0)
    val date=parser.parse(value,position) ?: return value
    if(position.index!=value.length) return value
    return SimpleDateFormat(if(locale.language=="es") "d MMM yyyy" else "MMM d, yyyy",locale).apply {timeZone=utc}.format(date)
}
