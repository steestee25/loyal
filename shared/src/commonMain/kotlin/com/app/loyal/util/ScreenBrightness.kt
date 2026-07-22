package com.app.loyal.util

import androidx.compose.runtime.Composable

/**
 * Finché è nella composizione porta lo schermo alla luminosità massima e ne
 * impedisce lo spegnimento: i lettori di codici a barre faticano con schermi
 * scuri, ed è fastidioso vedere il display spegnersi mentre si è in cassa.
 * Allo smontaggio ripristina le impostazioni di sistema.
 */
@Composable
expect fun KeepScreenBright()
