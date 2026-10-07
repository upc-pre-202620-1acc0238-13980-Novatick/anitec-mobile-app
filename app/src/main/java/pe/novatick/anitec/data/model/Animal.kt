package pe.novatick.anitec.data.model

import pe.novatick.anitec.R

enum class AnimalCategory(val label: String) {
    ALL("Todos"),
    BOVINE("Bovinos"),
    OVINE("Ovinos"),
    CAPRINE("Caprinos"),
    SWINE("Porcinos"),
    EQUINE("Equinos")
}

data class Animal(
    val id: String,
    val name: String,
    val code: String,
    val typeLabel: String,
    val category: AnimalCategory,
    val iconRes: Int,
    val status: String = "Activo"
)

// Listado de 20 animales de prueba
val sampleAnimalsList = listOf(
    Animal("1", "Luna", "BOV-024", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("2", "Mora", "OV-031", "Ovino", AnimalCategory.OVINE, R.drawable.ic_sheep),
    Animal("3", "Rayo", "EQ-008", "Equino", AnimalCategory.EQUINE, R.drawable.ic_horse),
    Animal("4", "Gloria", "BOV-015", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("5", "Laura", "BOV-042", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("6", "Polo", "CAP-012", "Caprino", AnimalCategory.CAPRINE, R.drawable.ic_goat),
    Animal("7", "Rosita", "POR-005", "Porcino", AnimalCategory.SWINE, R.drawable.ic_pig),
    Animal("8", "Bella", "BOV-009", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("9", "Bruno", "EQ-014", "Equino", AnimalCategory.EQUINE, R.drawable.ic_horse),
    Animal("10", "Copito", "OV-018", "Ovino", AnimalCategory.OVINE, R.drawable.ic_sheep),
    Animal("11", "Niebla", "CAP-003", "Caprino", AnimalCategory.CAPRINE, R.drawable.ic_goat),
    Animal("12", "Peppa", "POR-011", "Porcino", AnimalCategory.SWINE, R.drawable.ic_pig),
    Animal("13", "Sol", "BOV-053", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("14", "Tornado", "EQ-022", "Equino", AnimalCategory.EQUINE, R.drawable.ic_horse),
    Animal("15", "Motita", "OV-045", "Ovino", AnimalCategory.OVINE, R.drawable.ic_sheep),
    Animal("16", "Chiva", "CAP-027", "Caprino", AnimalCategory.CAPRINE, R.drawable.ic_goat),
    Animal("17", "Pumba", "POR-019", "Porcino", AnimalCategory.SWINE, R.drawable.ic_pig),
    Animal("18", "Estrella", "BOV-031", "Bovino", AnimalCategory.BOVINE, R.drawable.ic_cow),
    Animal("19", "Trueno", "EQ-003", "Equino", AnimalCategory.EQUINE, R.drawable.ic_horse),
    Animal("20", "Blanquita", "OV-007", "Ovino", AnimalCategory.OVINE, R.drawable.ic_sheep)
)