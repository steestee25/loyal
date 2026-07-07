package com.app.loyal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.loyal.model.LoyaltyCard
import coil3.compose.AsyncImage
import com.app.loyal.data.brandLogoUrl

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    onAddClick: () -> Unit,
    onCardClick: (LoyaltyCard) -> Unit
) {
    val cards by viewModel.cards.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(cards) { card ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        AsyncImage(
                            model = card.logoUrl,
                            contentDescription = card.brandName,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(card.brandName)
                        Text(card.code)
                    }
                    Button(onClick = { onCardClick(card) }) {
                        Text("Apri")
                    }
                }
            }
        }
    }
}
