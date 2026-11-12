package pe.novatick.anitec.features.livestock.domain
import java.text.SimpleDateFormat
import java.text.ParsePosition
import java.util.*

data class CareSummary(val id:String,val title:String,val vetName:String,val date:String,val summary:String)
data class BasicCare(val id:String,val animalId:String,val title:String,val description:String,val date:String,val veterinarianId:String,val vetName:String)
data class NewBasicCare(val animalId:String,val title:String,val description:String,val date:String)
fun careDateLabel(value:String,locale:Locale):String {
 val zone=TimeZone.getTimeZone("UTC")
 val date=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{timeZone=zone;isLenient=false}.parse(value)!!
 return SimpleDateFormat(if(locale.language=="es")"d MMM yyyy" else "MMM d, yyyy",locale).apply{timeZone=zone}.format(date)
}
fun validCareDate(value:String):Boolean {
 val parser=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply{isLenient=false;timeZone=TimeZone.getTimeZone("UTC")};val pos=ParsePosition(0)
 return value.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) && parser.parse(value,pos)!=null && pos.index==value.length
}
fun careInputValid(input:NewBasicCare,today:String=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))=
 runCatching{UUID.fromString(input.animalId).toString()==input.animalId.lowercase(Locale.ROOT)}.getOrDefault(false) &&
 input.title.isNotBlank() && input.title.length<=150 && input.description.isNotBlank() && validCareDate(input.date) && input.date<=today
