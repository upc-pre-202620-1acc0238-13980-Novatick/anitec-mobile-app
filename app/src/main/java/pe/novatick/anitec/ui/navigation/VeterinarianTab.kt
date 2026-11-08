package pe.novatick.anitec.ui.navigation

import pe.novatick.anitec.R

enum class VeterinarianTab(val title: String, val iconRes: Int) {
    HOME("Inicio", R.drawable.ic_home),
    AGENDA("Agenda", R.drawable.ic_calendar),
    RANCHERS("Ganaderos", R.drawable.ic_users),
    PROFILE("Mi cuenta", R.drawable.ic_user)
}