package com.app.loyal.util

import androidx.compose.runtime.Composable

/** iOS non ha un tasto indietro di sistema: no-op. */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}
