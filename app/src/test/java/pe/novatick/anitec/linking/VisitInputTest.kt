package pe.novatick.anitec.linking
import org.junit.Test
import org.junit.Assert.*
import pe.novatick.anitec.features.linking.domain.*
import java.util.TimeZone
class VisitInputTest {
 @Test fun localPeruTimeUsesUtcWithoutMovingTheCivilDay(){val zone=TimeZone.getTimeZone("America/Lima");val millis=visitLocalMillis("2099-01-02","10:30",zone)!!;assertEquals("2099-01-02T15:30:00Z",VisitDay.current(millis,zone).utc(millis))}
 @Test fun invalidCalendarDatesTimesAndDstGapsAreRejected(){for(pair in listOf("2026-02-30" to "10:00","2026-01-01" to "24:00","2026-01-01" to "10:60"))assertNull(visitLocalMillis(pair.first,pair.second));assertNull(visitLocalMillis("2026-03-08","02:30",TimeZone.getTimeZone("America/New_York")))}
 @Test fun quarterHourZoneAndMidnightAreConvertedCorrectly(){val zone=TimeZone.getTimeZone("Asia/Kathmandu");val millis=visitLocalMillis("2099-01-02","00:00",zone)!!;assertEquals("2099-01-01T18:15:00Z",VisitDay.current(millis,zone).utc(millis))}
 @Test fun missingContextReasonLimitAndFutureAreEnforced(){val zone=TimeZone.getTimeZone("UTC");val at=visitLocalMillis("2099-01-02","10:30",zone)!!;assertTrue(visitInputValid("o","a","x".repeat(255),"2099-01-02","10:30",0,zone));assertFalse(visitInputValid("o","a","x".repeat(256),"2099-01-02","10:30",0,zone));assertFalse(visitInputValid(null,"a","x","2099-01-02","10:30",0,zone));assertFalse(visitInputValid("o","a"," ","2099-01-02","10:30",0,zone));assertFalse(visitInputValid("o","a","x","2099-01-02","10:30",at,zone))}
}
