package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

// Shape curvo personalizado para el encabezado de la pantalla de Registro
val RegisterHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.65f) // Borde derecho superior

    // Curva que desciende hacia la izquierda manteniendo espacio para el título
    cubicTo(
        size.width * 0.65f, size.height * 0.82f,
        size.width * 0.35f, size.height * 0.98f,
        0f, size.height * 0.78f
    )
    close()
}