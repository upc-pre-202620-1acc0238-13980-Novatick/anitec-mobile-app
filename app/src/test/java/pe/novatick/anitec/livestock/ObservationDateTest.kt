package pe.novatick.anitec.livestock
import org.junit.Test
import org.junit.Assert.*
import java.util.*
import pe.novatick.anitec.features.livestock.presentation.formatObservationDate
class ObservationDateTest {
 @Test fun civilDateDoesNotShiftWithDeviceZone() {
  val previous=TimeZone.getDefault()
  try {TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Apia"));assertEquals("10 nov 2026",formatObservationDate("2026-11-10",Locale("es")));assertEquals("Nov 10, 2026",formatObservationDate("2026-11-10",Locale.ENGLISH));assertEquals("invalid",formatObservationDate("invalid",Locale.ENGLISH))} finally {TimeZone.setDefault(previous)}
 }
}
