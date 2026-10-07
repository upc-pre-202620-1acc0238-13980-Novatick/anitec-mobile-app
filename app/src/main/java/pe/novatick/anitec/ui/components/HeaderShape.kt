package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

// Curva que se extiende hacia abajo en el centro
val CurvedHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.76f) // Lado derecho

    // Curva hacia abajo teniendo el punto de control en el centro inferior
    quadraticTo(
        size.width / 2f, size.height, // Centro inferior
        0f, size.height * 0.76f       // Lado izquierdo
    )
    close()
}