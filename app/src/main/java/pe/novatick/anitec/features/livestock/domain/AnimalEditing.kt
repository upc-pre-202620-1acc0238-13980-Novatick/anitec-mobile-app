package pe.novatick.anitec.features.livestock.domain

/** PUT snapshot: farm is immutable; keep all optional fields, including photoUrl. */
data class AnimalEdit(val code: String,val speciesId: String,val sex: AnimalSex,val name: String?,
    val breed: String?,val birthDate: String?,val photoUrl: String?,val version: Long) {
    companion object { fun from(a: AnimalDetail)=AnimalEdit(a.code,a.speciesId,a.sex,a.name,a.breed,a.birthDate,a.photoUrl,a.version) }
}
fun animalEditErrors(base: AnimalDetail,input: AnimalEdit): Set<String> = animalInputErrors(
    NewAnimal(base.farmId,input.speciesId,input.sex,input.code,input.name,input.breed,input.birthDate,input.photoUrl)) + buildSet {
        if(input.code.isBlank()) add("code")
        if(input.version<0 || input.version!=base.version) add("version")
    }
data class AnimalDeactivation(val id: String,val status: AnimalStatus)
