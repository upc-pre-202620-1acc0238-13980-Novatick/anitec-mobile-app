package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

val VerifyHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.78f)

    // Curva suave hacia el centro inferior
    quadraticTo(
        size.width / 2f, size.height,
        0f, size.height * 0.78f
    )
    close()
}