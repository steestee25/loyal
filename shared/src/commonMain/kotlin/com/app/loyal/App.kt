package com.app.loyal

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.loyal.data.InMemoryLoyaltyCardRepository
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
sealed class Screen {
    data object List : Screen()
    data object AddCard : Screen()
    data class Detail(val card: LoyaltyCard) : Screen()
}

@Composable
fun App() {
    val repository = remember { InMemoryLoyaltyCardRepository() }
    val viewModel = remember { CardListViewModel(repository) }
    val scope = rememberCoroutineScope()

    var screen by remember { mutableStateOf<Screen>(Screen.List) }

    MaterialTheme {
        when (val current = screen) {
            is Screen.List -> CardListScreen(
                viewModel = viewModel,
                onAddClick = { screen = Screen.AddCard },
                onCardClick = { card -> screen = Screen.Detail(card) }
            )
            is Screen.AddCard -> AddEditCardScreen(
                onSave = { card ->
                    scope.launch { repository.add(card) }
                    screen = Screen.List
                },
                onCancel = { screen = Screen.List }
            )
            is Screen.Detail -> CardDetailScreen(
                card = current.card,
                onDelete = {
                    viewModel.deleteCard(current.card.id)
                    screen = Screen.List
                },
                onBack = { screen = Screen.List }
            )
        }
    }
}
