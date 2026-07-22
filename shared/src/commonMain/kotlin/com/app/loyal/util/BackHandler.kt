package com.app.loyal.util

import androidx.compose.runtime.Composable

/**
 * Intercetta il gesto "indietro" di sistema finché è nella composizione.
 * Ha effetto solo su Android: altrove il concetto non esiste ed è un no-op.
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
