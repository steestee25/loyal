package com.app.loyal.data

import com.app.loyal.i18n.AppLanguage
import com.app.loyal.ui.CardSortOrder
import com.app.loyal.util.KeyValueStore

/**
 * Preferenze dell'utente che devono sopravvivere alla chiusura dell'app.
 * Valori sconosciuti (o scritti da una versione futura) ricadono sul default.
 */
class AppPreferences(private val store: KeyValueStore) {

    var language: AppLanguage
        get() = store.getString(KEY_LANGUAGE)
            ?.let { code -> AppLanguage.entries.firstOrNull { it.code == code } }
            ?: AppLanguage.Italian
        set(value) = store.putString(KEY_LANGUAGE, value.code)

    var sortOrder: CardSortOrder
        get() = store.getString(KEY_SORT_ORDER)
            ?.let { name -> CardSortOrder.entries.firstOrNull { it.name == name } }
            ?: CardSortOrder.Alphabetical
        set(value) = store.putString(KEY_SORT_ORDER, value.name)

    private companion object {
        const val KEY_LANGUAGE = "language"
        const val KEY_SORT_ORDER = "sort_order"
    }
}
