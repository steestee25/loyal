package com.app.loyal.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class SharedPreferencesStore(
    private val prefs: SharedPreferences
) : KeyValueStore {
    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore {
    // applicationContext: l'archivio sopravvive alla singola Activity.
    val context = LocalContext.current.applicationContext
    return remember(context) {
        SharedPreferencesStore(context.getSharedPreferences("loyal", Context.MODE_PRIVATE))
    }
}
