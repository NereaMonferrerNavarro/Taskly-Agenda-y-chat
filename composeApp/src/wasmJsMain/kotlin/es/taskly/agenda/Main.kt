package es.taskly.agenda

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) { App() }
}

actual fun reproducirAviso() {
    // Web-> el navegador exige un gesto del usuario para audio, se deja sin sonido.
}
