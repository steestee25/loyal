package com.app.loyal.util

import androidx.compose.runtime.Composable

/** Su desktop non c'è un tasto indietro di sistema: no-op. */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}
