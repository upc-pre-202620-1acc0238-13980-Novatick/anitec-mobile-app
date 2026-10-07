package pe.novatick.anitec.ui.components

import androidx.compose.foundation.shape.GenericShape

// Curva personalizada para el encabezado de la pantalla de Login
val LoginHeaderShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.60f) // Finaliza más arriba en el lado derecho

    // Curva cúbica Bézier que baja debajo del texto "Iniciar Sesión" a la izquierda
    cubicTo(
        size.width * 0.55f, size.height * 0.70f, // Punto de control derecho
        size.width * 0.25f, size.height * 0.95f, // Punto de control izquierdo (panza)
        0f, size.height * 0.68f                  // Punto final en el borde izquierdo
    )
    close()
}