package com.app.loyal.util

/**
 * Colore rappresentativo (ARGB, opaco) di un'immagine logo codificata (PNG/JPG/...).
 * Restituisce la media dei pixel non trasparenti, o null se non decodificabile.
 */
expect fun averageColorArgb(imageBytes: ByteArray): Long?
