package pe.novatick.anitec.debug

import kotlinx.coroutines.awaitCancellation
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import java.util.UUID

/** Debug source set only. In-memory QA data; no clients, secrets or persistent session. */
internal class DebugLivestockRepository(val scenario: String="content") : LivestockRepository {
    private fun uuid(n: Int)=UUID(0L,n.toLong()).toString()
    val owner=uuid(1)
    val catalog=listOf("Bovino","Ovino","Caprino","Porcino","Equino").mapIndexed { index,name ->Species(uuid(10+index),name,null,null) }
    private val mutableFarms=mutableListOf(Farm(uuid(2),"Finca Esperanza","Sector norte · Arequipa",owner,"2035-01-01T00:00:00Z"),
        Farm(uuid(3),"Finca El Olivar","Ubicación de prueba",owner,"2035-01-01T00:00:00Z"))
    private val names=listOf("Luna","Mora","Rayo","Gloria","Laura","Polo","Alba","Nina","Sol","Brisa","Perla","Rocío","Cielo","Nube","Valle")
    private val animals=names.mapIndexed { i,name ->AnimalSummary(uuid(100+i),"ANI-"+(i+1).toString().padStart(6,'0'),name,catalog[i%catalog.size].name,catalog[i%catalog.size].id,
        if(i<12) AnimalStatus.ACTIVO else AnimalStatus.entries[i-11],mutableFarms[if(i<8) 0 else 1].id,owner,0) }.toMutableList()
    private val registered=mutableMapOf<String,AnimalDetail>()
    override suspend fun createAnimal(input: NewAnimal): AnimalSummary {
        if(animalInputErrors(input).isNotEmpty()) throw AuthFailure(AuthProblem.SERVER,400,"VALIDATION_ERROR")
        if(scenario=="save-uncertain") throw AuthFailure(AuthProblem.NETWORK)
        if(scenario=="save-error") throw AuthFailure(AuthProblem.SERVER,409,"DUPLICATE_ANIMAL_CODE")
        if(capacity().remaining==0L) throw AuthFailure(AuthProblem.SERVER,409,"INVENTORY_LIMIT_REACHED")
        val code=input.code?.trim()?.takeIf(String::isNotEmpty) ?: ("QA-"+(animals.size+1))
        if(animals.any {it.code.equals(code,true)}) throw AuthFailure(AuthProblem.SERVER,409,"DUPLICATE_ANIMAL_CODE")
        val species=catalog.first {it.id==input.speciesId};val farm=mutableFarms.first {it.id==input.farmId}
        val result=AnimalSummary(uuid(100+animals.size),code,input.name,species.name,species.id,AnimalStatus.ACTIVO,farm.id,owner,0)
        animals.add(result)
        registered[result.id]=AnimalDetail(result.id,code,input.name,species.name,species.id,input.sex,input.breed,input.birthDate,result.status,farm.id,farm.name,0,null)
        return result
    }
    override suspend fun animal(id: String): AnimalDetail {
        read("detail")
        return registered[id] ?: animals.firstOrNull {it.id==id}?.let {
            AnimalDetail(it.id,it.code,it.name,it.species,it.speciesId,AnimalSex.DESCONOCIDO,null,null,it.status,it.farmId,mutableFarms.first {f->f.id==it.farmId}.name,it.version,null)
        } ?: throw AuthFailure(AuthProblem.SERVER,404,"NOT_FOUND")
    }
    private val failed=mutableSetOf<String>()
    var writes=0
    val reads=mutableMapOf<String,Int>()
    private suspend fun read(kind: String) {
        reads[kind]=reads.getOrDefault(kind,0)+1
        if(scenario=="loading") awaitCancellation()
        if(scenario=="error" && failed.add(kind)) throw AuthFailure(AuthProblem.NETWORK)
    }
    private val dataset get()=if(scenario in listOf("empty","no-farms")) animals.filter {it.id in registered} else animals
    override suspend fun farms(): List<Farm> {read("farms");return if(scenario=="no-farms") mutableFarms.drop(2) else mutableFarms.toList()}
    override suspend fun farm(id: String): Farm {read("farm");return mutableFarms.first {it.id==id}}
    override suspend fun species(): List<Species> {read("species");return catalog}
    override suspend fun capacity(): Capacity {read("capacity");return Capacity(owner,15,dataset.count {it.status==AnimalStatus.ACTIVO}.toLong(),0)}
    override suspend fun inventory(filter: InventoryFilter): Inventory {
        read("inventory")
        val scope=dataset.filter {filter.farmId==null || it.farmId==filter.farmId}
        return Inventory(scope.count {it.status==AnimalStatus.ACTIVO}.toLong(),scope.filter {
            (filter.species==null || it.speciesId==filter.species) && (filter.status==null || it.status==filter.status) &&
                (filter.search.isBlank() || it.code.contains(filter.search,true) || it.name.orEmpty().contains(filter.search,true))
        })
    }
    override suspend fun createFarm(name: String,location: String?): Farm {
        writes++
        // A bounded fake delay lets a reviewer inspect busy controls without being trapped in a modal.
        if(scenario=="saving") {kotlinx.coroutines.delay(3000);throw AuthFailure(AuthProblem.SERVER,503,"QA_DELAY_COMPLETE")}
        if(scenario=="save-uncertain") throw AuthFailure(AuthProblem.NETWORK)
        if(scenario=="save-error" && writes==1) throw AuthFailure(AuthProblem.SERVER,503,"UNAVAILABLE")
        return Farm(uuid(40+writes),name,location,owner,"2035-01-01T00:00:00Z").also {mutableFarms+=it}
    }
}
