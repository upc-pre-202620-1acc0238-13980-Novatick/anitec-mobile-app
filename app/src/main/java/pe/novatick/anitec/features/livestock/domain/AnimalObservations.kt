package pe.novatick.anitec.features.livestock.domain

/** Server supplies author/date; date is a UTC-projected civil date, never a device instant. */
data class AnimalObservation(val id: String,val text: String,val authorName: String?,val date: String?)
fun validObservationText(text: String)=text.isNotBlank()
