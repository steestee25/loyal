package com.app.loyal.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

private class PreferencesStore(
    private val prefs: Preferences
) : KeyValueStore {
    override fun getString(key: String): String? = prefs.get(key, null)

    override fun putString(key: String, value: String) {
        prefs.put(key, value)
    }

    override fun remove(key: String) {
        prefs.remove(key)
    }
}

@Composable
actual fun rememberKeyValueStore(): KeyValueStore = remember {
    PreferencesStore(Preferences.userRoot().node("com/app/loyal"))
}
