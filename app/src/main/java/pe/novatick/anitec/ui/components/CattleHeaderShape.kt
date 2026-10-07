package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

val CattleHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height)

    // Curva cóncava verde hacia arriba (el centro sube)
    quadraticTo(
        size.width / 2f, size.height * 0.35f, // Control en el centro, atrayendo la curva hacia arriba
        0f, size.height                        // Fin en la esquina inferior izquierda
    )
    close()
}