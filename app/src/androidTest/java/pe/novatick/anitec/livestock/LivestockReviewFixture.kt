package pe.novatick.anitec.livestock

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.awaitCancellation
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.screens.home.RancherMainContainer
import pe.novatick.anitec.ui.theme.AniTecTheme
import java.util.Locale
import java.util.UUID

/** Instrumentation-only, coherent data. No Retrofit/client, accounts, HTTP or email. */
internal class LivestockReviewRepository(val scenario: String="content") : LivestockRepository {
    private fun uuid(n: Int)=UUID(0L,n.toLong()).toString()
    val owner=uuid(1)
    val catalog=listOf("Bovino","Ovino","Caprino","Porcino","Equino").mapIndexed { index,name ->Species(uuid(10+index),name,null,null) }
    private val mutableFarms=mutableListOf(Farm(uuid(2),"Finca Esperanza","Sector norte · Arequipa",owner,"2035-01-01T00:00:00Z"),
        Farm(uuid(3),"Finca El Olivar","Ubicación de prueba",owner,"2035-01-01T00:00:00Z"))
    private val names=listOf("Luna","Mora","Rayo","Gloria","Laura","Polo","Alba","Nina","Sol","Brisa","Perla","Rocío","Cielo","Nube","Valle")
    private val animals=names.mapIndexed { i,name ->AnimalSummary(uuid(100+i),"ANI-"+(i+1).toString().padStart(6,'0'),name,catalog[i%catalog.size].name,catalog[i%catalog.size].id,
        if(i<12) AnimalStatus.ACTIVO else AnimalStatus.entries[i-11],mutableFarms[if(i<8) 0 else 1].id,owner,0) }.toMutableList()
    private val registered=mutableMapOf<String,AnimalDetail>()
    val animalReads=mutableListOf<String>()
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
    var mutationWrites=0
    override suspend fun updateAnimal(id: String,input: AnimalEdit): AnimalSummary {
        mutationWrites++
        if(scenario=="saving") kotlinx.coroutines.delay(2500)
        if(scenario=="save-uncertain") throw AuthFailure(AuthProblem.NETWORK)
        if(scenario=="edit-conflict") throw AuthFailure(AuthProblem.SERVER,409,"ANIMAL_VERSION_CONFLICT")
        if(scenario=="save-error") throw AuthFailure(AuthProblem.SERVER,409,"DUPLICATE_ANIMAL_CODE")
        val old=animal(id)
        if(old.version!=input.version) throw AuthFailure(AuthProblem.SERVER,409,"ANIMAL_VERSION_CONFLICT")
        if(animals.any {it.id!=id && it.code.equals(input.code.trim(),true)}) throw AuthFailure(AuthProblem.SERVER,409,"DUPLICATE_ANIMAL_CODE")
        val species=catalog.first {it.id==input.speciesId}
        val detail=old.copy(code=input.code.trim().uppercase(),name=input.name,species=species.name,speciesId=species.id,
            sex=input.sex,breed=input.breed,birthDate=input.birthDate,photoUrl=input.photoUrl,version=old.version+1)
        registered[id]=detail
        val i=animals.indexOfFirst {it.id==id}
        val result=animals[i].copy(code=detail.code,name=detail.name,species=detail.species,speciesId=detail.speciesId,version=detail.version)
        animals[i]=result;return result
    }
    override suspend fun deactivateAnimal(id: String): AnimalDeactivation {
        mutationWrites++
        if(scenario=="saving") kotlinx.coroutines.delay(2500)
        if(scenario=="save-uncertain") throw AuthFailure(AuthProblem.NETWORK)
        if(scenario=="save-error") throw AuthFailure(AuthProblem.SERVER,403,"FORBIDDEN")
        val old=animal(id)
        registered[id]=old.copy(status=AnimalStatus.INACTIVO,version=old.version+1)
        val i=animals.indexOfFirst {it.id==id}
        animals[i]=animals[i].copy(status=AnimalStatus.INACTIVO,version=old.version+1)
        return AnimalDeactivation(id,AnimalStatus.INACTIVO)
    }
    override suspend fun animal(id: String): AnimalDetail {
        animalReads+=id
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
        if(scenario=="saving") awaitCancellation()
        if(scenario=="save-uncertain") throw AuthFailure(AuthProblem.NETWORK)
        if(scenario=="save-error" && writes==1) throw AuthFailure(AuthProblem.SERVER,503,"UNAVAILABLE")
        return Farm(uuid(40+writes),name,location,owner,"2035-01-01T00:00:00Z").also {mutableFarms+=it}
    }
}
@Composable internal fun LivestockLocaleFixture(language: String,repository: LivestockRepository) {
    val original=InstrumentationRegistry.getInstrumentation().targetContext
    val configuration=remember(language) {Configuration(original.resources.configuration).apply {setLocale(Locale.forLanguageTag(language));fontScale=1f}}
    val localized=remember(language) {original.createConfigurationContext(configuration)}
    val density=LocalDensity.current
    CompositionLocalProvider(LocalContext provides localized,LocalResources provides localized.resources,LocalConfiguration provides configuration,
        LocalDensity provides Density(density.density,1f)) {
        AniTecTheme {RancherMainContainer(livestock=repository,profile=Profile("00000000-0000-0000-0000-000000000001","review@example.invalid","Alex","Rivera",Role.GANADERO))}
    }
}
