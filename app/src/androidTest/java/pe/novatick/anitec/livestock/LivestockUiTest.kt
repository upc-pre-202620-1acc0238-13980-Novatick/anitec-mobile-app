package pe.novatick.anitec.livestock
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.*
import org.junit.Assert.*
import pe.novatick.anitec.features.auth.domain.*
import pe.novatick.anitec.features.livestock.domain.*
import pe.novatick.anitec.ui.screens.home.RancherMainContainer
import pe.novatick.anitec.ui.theme.AniTecTheme
class LivestockUiTest {
    @get:Rule val compose=createComposeRule()
    // Legacy contract fixture: independent responses (capacity=12, total=8, content=0).
    // Supervised visual review uses the coherent LivestockReviewRepository instead.
    private class Fake(val label: String): LivestockRepository {
        var creates=0;var last: InventoryFilter?=null
        val farm=Farm("22222222-2222-4222-8222-222222222222",label,null,"owner","2035-01-01T00:00:00Z")
        override suspend fun farms()=listOf(farm)
        override suspend fun farm(id: String)=farm
        override suspend fun createFarm(name: String,location: String?):Farm {creates++;return farm.copy(name=name,location=location)}
        override suspend fun species()=listOf(Species("33333333-3333-4333-8333-333333333333","Catalog species",null,null))
        override suspend fun inventory(filter: InventoryFilter):Inventory {last=filter;return Inventory(8,emptyList())}
        override suspend fun capacity()=Capacity("owner",15,12,0)
    }
    private fun text(id: Int,vararg args: Any): String {
        val resources=androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.resources
        return if(resources.getResourceTypeName(id)=="plurals") resources.getQuantityString(id,if((args[0] as Number).toLong()==1L) 1 else 2,*args) else resources.getString(id,*args)
    }
    private fun screen(fake: Fake) {
        compose.setContent { AniTecTheme {RancherMainContainer(livestock=fake,profile=Profile("owner","test@example.invalid","Test","User",Role.GANADERO))} }
        compose.onNodeWithText("Ganado").performClick()
    }
    @Test fun farmModalCancelDoesNotWriteAndRealSpeciesFilterUsesUuid() {
        val fake=Fake("Farm A");screen(fake)
        compose.onNodeWithText("Mis fincas").performScrollTo().performClick()
        compose.onNodeWithText("Agregar finca").performClick()
        compose.onNodeWithTag("farm_name").performTextInput("Draft")
        compose.onNodeWithText("Cancelar").performClick()
        compose.runOnIdle {assertEquals(0,fake.creates)}
        compose.onNodeWithText("Cerrar").performClick()
        compose.onNodeWithText("Todas las especies").performScrollTo().performClick()
        compose.onNodeWithText("Catalog species").performClick()
        compose.runOnIdle {assertEquals("33333333-3333-4333-8333-333333333333",fake.last!!.species)}
        compose.onNodeWithText(text(pe.novatick.anitec.R.plurals.ls_active_global_count,8L)).performScrollTo().assertExists()
        compose.onNodeWithText(text(pe.novatick.anitec.R.plurals.ls_result_count,0)).performScrollTo().assertExists()
    }
    @Test fun farmCreationReturnsServerIdentityAndSelectsContext() {
        val fake=Fake("Farm A");screen(fake)
        compose.onNodeWithText("Mis fincas").performScrollTo().performClick();compose.onNodeWithText("Agregar finca").performClick()
        compose.onNodeWithTag("farm_name").performTextInput("New farm")
        compose.onNodeWithText("Guardar").performClick()
        compose.waitUntil(5000) {fake.last?.farmId==fake.farm.id}
        compose.runOnIdle {assertEquals(1,fake.creates)}
        compose.onNodeWithText(text(pe.novatick.anitec.R.plurals.ls_active_farm_count,8L)).performScrollTo().assertExists()
    }
    @Test fun accountChangeDisposesPrivateFiltersAndNeverShowsPreviousFarm() {
        val first=Fake("PRIVATE-A");val second=Fake("PRIVATE-B");var b by mutableStateOf(false)
        compose.setContent { AniTecTheme {RancherMainContainer(livestock=if(b) second else first,
            profile=Profile(if(b) "B" else "A","test@example.invalid","Test","User",Role.GANADERO))} }
        compose.onNodeWithText("Ganado").performClick();compose.onNodeWithTag("livestock_search").performTextInput("secret-search")
        compose.runOnIdle {b=true}
        compose.onNodeWithText("Ganado").performClick()
        compose.onNodeWithTag("livestock_search").assertTextContains("")
        compose.runOnIdle {assertEquals("",second.last!!.search)}
        compose.onNodeWithText("PRIVATE-A").assertDoesNotExist()
    }
    @Test fun visualInventoryReviewWithControlledFarmAndCatalog() {
        screen(Fake("Controlled QA farm"))
        compose.onNodeWithContentDescription(text(pe.novatick.anitec.R.string.ls_capacity,12L,15L,3L)).assertExists()
        // Paused manual review has its own command-driven test; no timed navigation here.
    }
}
