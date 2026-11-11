package pe.novatick.anitec.features.livestock.domain
import java.text.SimpleDateFormat
import java.util.*
enum class AnimalSex { MACHO, HEMBRA, DESCONOCIDO }
data class NewAnimal(val farmId: String,val speciesId: String,val sex: AnimalSex,val code: String?=null,
    val name: String?=null,val breed: String?=null,val birthDate: String?=null,val photoUrl: String?=null)
data class AnimalDetail(val id: String,val code: String,val name: String?,val species: String,val speciesId: String,
    val sex: AnimalSex,val breed: String?,val birthDate: String?,val status: AnimalStatus,val farmId: String,val farmName: String,
    val version: Long,val photoUrl: String?)
fun animalInputErrors(input: NewAnimal,today: String=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())): Set<String> = buildSet {
    fun uuid(v: String)=runCatching {UUID.fromString(v).toString()==v.lowercase(Locale.ROOT)}.getOrDefault(false)
    if(!uuid(input.farmId)) add("farmId")
    if(!uuid(input.speciesId)) add("speciesId")
    input.code?.trim()?.takeIf {it.isNotEmpty()}?.let {if(!it.matches(Regex("[A-Za-z0-9-]{1,30}")) || !it.any {c->c.isLetterOrDigit()}) add("code")}
    if((input.name?.length ?: 0)>100) add("name")
    if((input.breed?.length ?: 0)>100) add("breed")
    input.birthDate?.let {
        val parser=SimpleDateFormat("yyyy-MM-dd",Locale.US).apply {isLenient=false;timeZone=TimeZone.getTimeZone("UTC")}
        val pos=java.text.ParsePosition(0)
        if(!it.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) || parser.parse(it,pos)==null || pos.index!=it.length || it>today) add("birthDate")
    }
}
