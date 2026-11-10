package pe.novatick.anitec.features.livestock.presentation

import pe.novatick.anitec.R
import java.util.Locale

/** Presentation-only mapping of the official catalog names. Never infer species from a UUID or substring. */
internal fun livestockSpeciesIcon(name: String): Int = when(name.trim().lowercase(Locale.ROOT)) {
    "bovino" -> R.drawable.ls_species_bovino
    "ovino" -> R.drawable.ls_species_ovino
    "caprino" -> R.drawable.ls_species_caprino
    "porcino" -> R.drawable.ls_species_porcino
    "equino" -> R.drawable.ls_species_equino
    else -> R.drawable.ic_folder
}

internal fun livestockInitials(name: String,lastName: String): String =
    listOf(name,lastName).mapNotNull { part -> part.trim().takeIf(String::isNotEmpty)?.let {
        String(Character.toChars(it.codePointAt(0))).uppercase(Locale.ROOT)
    } }.joinToString("").ifEmpty { "?" }
