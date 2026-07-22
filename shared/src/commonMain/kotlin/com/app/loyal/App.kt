package com.app.loyal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.loyal.data.SupabaseLoyaltyCardRepository
import com.app.loyal.model.LoyaltyCard
import com.app.loyal.ui.AddEditCardScreen
import com.app.loyal.ui.CardListScreen
import com.app.loyal.ui.CardListViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.app.loyal.barcode.BarcodeView
import com.app.loyal.barcode.CardDetailScreen
import com.app.loyal.barcode.Code128Encoder
import com.app.loyal.barcode.Ean13Encoder
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.app.loyal.data.createHttpClient
import com.app.loyal.data.BrandSearchApi
import com.app.loyal.data.createAppSupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.CompositionLocalProvider
import com.app.loyal.i18n.AppLanguage
import com.app.loyal.i18n.LocalStrings
import com.app.loyal.i18n.stringsFor
import com.app.loyal.ui.AuthScreen

sealed class Screen {
    data object List : Screen()
    data object AddCard : Screen()
    data class EditCard(val card: LoyaltyCard) : Screen()
    data class Detail(val card: LoyaltyCard) : Screen()
}

@Composable
fun App() {
    val supabase = remember { createAppSupabaseClient() }
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()

    val httpClient = remember { createHttpClient() }
    val brandSearchApi = remember { BrandSearchApi(httpClient) }

    val scope = rememberCoroutineScope()

    var screen by remember { mutableStateOf<Screen>(Screen.List) }
    var language by remember { mutableStateOf(AppLanguage.Italian) }
    val cardListState = rememberLazyListState()

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }
    MaterialTheme {
      CompositionLocalProvider(LocalStrings provides stringsFor(language)) {
        val status = sessionStatus
        val user = (status as? SessionStatus.Authenticated)?.session?.user
        val userId = user?.id
        val userEmail = user?.email ?: ""

        if (userId != null) {
            val repository = remember(userId) { SupabaseLoyaltyCardRepository(supabase, userId) }
            val viewModel = remember(userId) { CardListViewModel(repository) }

            LaunchedEffect(userId) { repository.refresh() }

            when (val current = screen) {
                is Screen.List -> CardListScreen(
                    viewModel = viewModel,
                    userEmail = userEmail,
                    supabase = supabase,
                    language = language,
                    onLanguageChange = { language = it },
                    onAddClick = { screen = Screen.AddCard },
                    onCardClick = { card -> screen = Screen.Detail(card) },
                    onLogoutClick = {
                        scope.launch { supabase.auth.signOut() }
                        screen = Screen.List
                    },
                    listState = cardListState
                )
                is Screen.AddCard -> AddEditCardScreen(
                    brandSearchApi = brandSearchApi,
                    onSave = { card ->
                        scope.launch { repository.add(card) }
                        screen = Screen.List
                    },
                    onCancel = { screen = Screen.List }
                )
                is Screen.EditCard -> AddEditCardScreen(
                    brandSearchApi = brandSearchApi,
                    initialCard = current.card,
                    onSave = { card ->
                        scope.launch { repository.update(card) }
                        screen = Screen.List
                    },
                    onCancel = { screen = Screen.Detail(current.card) }
                )
                is Screen.Detail -> CardDetailScreen(
                    card = current.card,
                    onEdit = { screen = Screen.EditCard(current.card) },
                    onDelete = {
                        viewModel.deleteCard(current.card.id)
                        screen = Screen.List
                    },
                    onToggleFavorite = {
                        viewModel.toggleFavorite(current.card)
                        // Screen.Detail tiene uno snapshot della carta: lo aggiorniamo
                        // a mano perché il cuore nel menu rifletta subito il nuovo stato.
                        screen = Screen.Detail(
                            current.card.copy(isFavorite = !current.card.isFavorite)
                        )
                    },
                    onBack = { screen = Screen.List }
                )
            }
        } else {
            AuthScreen(supabase = supabase)
        }
      }
    }
}
