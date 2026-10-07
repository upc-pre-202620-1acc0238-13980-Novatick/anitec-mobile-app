package pe.novatick.anitec.ui.navigation

import pe.novatick.anitec.R

enum class RancherTab(
    val title: String,
    val iconRes: Int
) {
    HOME("Inicio", R.drawable.ic_home),
    CATTLE("Ganado", R.drawable.ic_cow),
    VETS("Veterinarios", R.drawable.ic_stethoscope),
    PROFILE("Mi cuenta", R.drawable.ic_user)
}