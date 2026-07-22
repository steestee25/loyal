package com.app.loyal.i18n

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Lingue supportate dall'app. [code] è il codice ISO, [displayName] è il nome
 * mostrato nel selettore (sempre nella lingua stessa, come da convenzione).
 */
enum class AppLanguage(val code: String, val displayName: String) {
    Italian("it", "Italiano"),
    English("en", "English")
}

/**
 * Contenitore di tutte le stringhe visibili dell'app. Ogni lingua è un'istanza
 * di [Strings]; la lingua attiva viene fornita tramite [LocalStrings] e cambiata
 * a runtime, così l'intera UI si ricompone nella lingua scelta.
 */
data class Strings(
    // Navigazione
    val navHome: String,
    val navSettings: String,
    // Home / ricerca / ordinamento
    val searchPlaceholder: String,
    val sortBy: String,
    val sortAlphabetical: String,
    val sortMostUsed: String,
    val sortRecentlyViewed: String,
    // Impostazioni
    val settingsTitle: String,
    val changePassword: String,
    val preferences: String,
    val logout: String,
    val newPassword: String,
    val save: String,
    val language: String,
    val passwordUpdated: String,
    // Autenticazione
    val login: String,
    val enterEmailTitle: String,
    val email: String,
    val next: String,
    val enterPasswordTitle: String,
    val password: String,
    val register: String,
    val registrationMessage: String,
    // Aggiungi / modifica carta
    val searchBrand: String,
    val details: String,
    val cardName: String,
    val notes: String,
    // Dettaglio carta
    val edit: String,
    val delete: String,
    // Preferiti
    val addToFavorites: String,
    val removeFromFavorites: String,
    /** Messaggio di limite raggiunto; riceve il numero massimo di preferiti. */
    val favoritesLimitReached: (Int) -> String,
    // Sezioni della home
    val favoritesSection: String,
    val cardsSection: String,
    // Stati vuoti
    val noCardsTitle: String,
    val noCardsMessage: String,
    val noSearchResultsTitle: String,
    val noSearchResultsMessage: String,
    // Conferma eliminazione
    val deleteCardTitle: String,
    val deleteCardMessage: String,
    val cancel: String,
    // Sincronizzazione
    val offlineChangesNotSynced: String,
)

private val ItalianStrings = Strings(
    navHome = "Home",
    navSettings = "Impostazioni",
    searchPlaceholder = "Cerca in Loyal",
    sortBy = "Ordina per",
    sortAlphabetical = "Ordine alfabetico",
    sortMostUsed = "Più usati",
    sortRecentlyViewed = "Visti di recente",
    settingsTitle = "Impostazioni",
    changePassword = "Cambia password",
    preferences = "Preferenze",
    logout = "Logout",
    newPassword = "Nuova password",
    save = "Salva",
    language = "Lingua",
    passwordUpdated = "Password aggiornata.",
    login = "Accedi",
    enterEmailTitle = "Inserisci la tua email",
    email = "Email",
    next = "Avanti",
    enterPasswordTitle = "Inserisci la password",
    password = "Password",
    register = "Registrati",
    registrationMessage = "Registrazione avvenuta. Se la conferma email è attiva su Supabase, controlla la posta prima di accedere.",
    searchBrand = "Cerca brand",
    details = "Dettagli",
    cardName = "Nome carta",
    notes = "Note",
    edit = "Modifica",
    delete = "Cancella",
    addToFavorites = "Aggiungi ai preferiti",
    removeFromFavorites = "Rimuovi dai preferiti",
    favoritesLimitReached = { max ->
        "Hai raggiunto il massimo di $max preferiti. Rimuovine uno per aggiungerne un altro."
    },
    favoritesSection = "Preferiti",
    cardsSection = "Carte",
    noCardsTitle = "Nessuna tessera",
    noCardsMessage = "Tocca + per aggiungere la tua prima tessera fedeltà.",
    noSearchResultsTitle = "Nessun risultato",
    noSearchResultsMessage = "Prova con un altro nome.",
    deleteCardTitle = "Eliminare la tessera?",
    deleteCardMessage = "La tessera verrà rimossa da tutti i tuoi dispositivi. L'operazione non è reversibile.",
    cancel = "Annulla",
    offlineChangesNotSynced = "Nessuna connessione: le tessere mostrate potrebbero non essere aggiornate.",
)

private val EnglishStrings = Strings(
    navHome = "Home",
    navSettings = "Settings",
    searchPlaceholder = "Search in Loyal",
    sortBy = "Sort by",
    sortAlphabetical = "Alphabetical",
    sortMostUsed = "Most used",
    sortRecentlyViewed = "Recently viewed",
    settingsTitle = "Settings",
    changePassword = "Change password",
    preferences = "Preferences",
    logout = "Log out",
    newPassword = "New password",
    save = "Save",
    language = "Language",
    passwordUpdated = "Password updated.",
    login = "Sign in",
    enterEmailTitle = "Enter your email",
    email = "Email",
    next = "Next",
    enterPasswordTitle = "Enter your password",
    password = "Password",
    register = "Sign up",
    registrationMessage = "Registration complete. If email confirmation is enabled on Supabase, check your inbox before signing in.",
    searchBrand = "Search brand",
    details = "Details",
    cardName = "Card name",
    notes = "Notes",
    edit = "Edit",
    delete = "Delete",
    addToFavorites = "Add to favorites",
    removeFromFavorites = "Remove from favorites",
    favoritesLimitReached = { max ->
        "You've reached the maximum of $max favorites. Remove one before adding another."
    },
    favoritesSection = "Favorites",
    cardsSection = "Cards",
    noCardsTitle = "No cards yet",
    noCardsMessage = "Tap + to add your first loyalty card.",
    noSearchResultsTitle = "No results",
    noSearchResultsMessage = "Try a different name.",
    deleteCardTitle = "Delete this card?",
    deleteCardMessage = "The card will be removed from all your devices. This can't be undone.",
    cancel = "Cancel",
    offlineChangesNotSynced = "You're offline: the cards shown may be out of date.",
)

/** Restituisce il set di stringhe per la [language] scelta. */
fun stringsFor(language: AppLanguage): Strings = when (language) {
    AppLanguage.Italian -> ItalianStrings
    AppLanguage.English -> EnglishStrings
}

/** Stringhe della lingua attiva. Default: italiano finché [App] non fornisce la lingua scelta. */
val LocalStrings = staticCompositionLocalOf { ItalianStrings }
