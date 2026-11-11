package pe.novatick.anitec.livestock
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.livestock.domain.*
class AnimalRegistrationTest {
    private val farm="22222222-2222-4222-8222-222222222222"
    private val species="33333333-3333-4333-8333-333333333333"
    @Test fun optionalCodeAndUnknownSexAreValidAndLimitsAreContractual() {
        val input=NewAnimal(farm,species,AnimalSex.DESCONOCIDO,name="a".repeat(100),breed="b".repeat(100))
        assertTrue(animalInputErrors(input).isEmpty())
        assertEquals(setOf("name","breed"),animalInputErrors(input.copy(name="a".repeat(101),breed="b".repeat(101))))
        assertEquals(setOf("code"),animalInputErrors(input.copy(code="---")))
        assertEquals(setOf("code"),animalInputErrors(input.copy(code="á")))
        assertTrue(animalInputErrors(input.copy(code="Z-001")).isEmpty())
        assertTrue(animalInputErrors(input.copy(code=" ")).isEmpty())
    }
    @Test fun civilDatesRejectMalformedImpossibleAndFutureWithoutTimeZoneConversion() {
        val input=NewAnimal(farm,species,AnimalSex.HEMBRA)
        for(date in listOf("2025-02-29","2024-13-01","01/01/2020","2024-03-02"))
            assertTrue(date,"birthDate" in animalInputErrors(input.copy(birthDate=date),"2024-03-01"))
        assertTrue(animalInputErrors(input.copy(birthDate="2024-02-29"),"2024-03-01").isEmpty())
    }
}
