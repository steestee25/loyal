package com.app.loyal.util

import androidx.compose.runtime.Composable

/**
 * Archivio chiave/valore persistente, usato per le preferenze (lingua, ordinamento)
 * e per la cache offline delle tessere. Volutamente minimale: solo stringhe.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

/**
 * Istanza dell'archivio per la piattaforma corrente.
 * È un composable perché su Android serve il [android.content.Context].
 */
@Composable
expect fun rememberKeyValueStore(): KeyValueStore
