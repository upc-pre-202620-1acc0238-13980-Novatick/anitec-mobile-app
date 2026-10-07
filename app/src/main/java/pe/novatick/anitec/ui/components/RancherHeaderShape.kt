package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

val RancherHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.82f)

    // Curva suave cóncava inferior
    quadraticTo(
        size.width / 2f, size.height,
        0f, size.height * 0.82f
    )
    close()
}