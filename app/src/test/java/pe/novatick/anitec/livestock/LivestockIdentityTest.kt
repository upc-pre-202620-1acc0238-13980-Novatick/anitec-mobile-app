package pe.novatick.anitec.livestock

import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.R
import pe.novatick.anitec.features.livestock.presentation.*

class LivestockIdentityTest {
    @Test fun officialSpeciesHaveDistinctIconsAndUnknownNamesStayNeutral() {
        val names=listOf("Bovino","Ovino","Caprino","Porcino","Equino")
        val icons=names.map(::livestockSpeciesIcon)
        assertEquals(5,icons.toSet().size)
        assertFalse(icons.contains(R.drawable.ic_folder))
        assertEquals(icons.first(),livestockSpeciesIcon(" BOVINO "))
        listOf("","Bovino desconocido","Mixto","Avícola").forEach {assertEquals(R.drawable.ic_folder,livestockSpeciesIcon(it))}
    }
    @Test fun initialsComeFromTheCurrentUserIncludingUnicodeAndMissingNames() {
        assertEquals("AR",livestockInitials("Alex","Rivera"))
        assertEquals("ÉÑ",livestockInitials(" Érika "," Ñahui "))
        assertEquals("A",livestockInitials("Alex",""))
        assertEquals("?",livestockInitials(" ",""))
    }
}
