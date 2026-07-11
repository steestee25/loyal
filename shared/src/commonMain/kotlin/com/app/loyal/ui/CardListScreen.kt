package com.app.loyal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.loyal.model.LoyaltyCard
import coil3.compose.AsyncImage

enum class Tab { Home, Settings }

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    onAddClick: () -> Unit,
    onCardClick: (LoyaltyCard) -> Unit,
    onLogoutClick: () -> Unit
) {
    val cards by viewModel.cards.collectAsState()
    var selectedTab by remember { mutableStateOf(Tab.Home) }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                Tab.Home -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item {
                        Button(
                            onClick = onAddClick,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Aggiungi carta")
                        }
                    }
                    items(cards) { card ->
                        LoyaltyCardItem(card = card, onClick = { onCardClick(card) })
                    }
                }

                Tab.Settings -> SettingsScreen(
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            FloatingNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSearchClick = {},
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun LoyaltyCardItem(
    card: LoyaltyCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Colori scuri/saturi (bassa luminosità) risultano troppo marcati:
    // li rendiamo più tenui abbassando l'alpha, mentre i colori chiari restano pieni.
    val baseColor = Color(card.colorArgb)
    val borderAlpha = (0.15f + 1.6f * baseColor.luminance()).coerceIn(0.15f, 1f)
    val borderColor = baseColor.copy(alpha = borderAlpha)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = card.logoUrl,
                contentDescription = card.brandName,
                modifier = Modifier.size(48.dp)
            )
            Column {
                Text(card.brandName)
                Text(card.code)
            }
        }
    }
}

@Composable
private fun FloatingNavBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pill con le voci di navigazione
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    label = "Home",
                    selected = selectedTab == Tab.Home,
                    onClick = { onTabSelected(Tab.Home) }
                )
                NavItem(
                    label = "Impostazioni",
                    selected = selectedTab == Tab.Settings,
                    onClick = { onTabSelected(Tab.Settings) }
                )
            }
        }

        // Lente di ricerca staccata
        Surface(
            onClick = onSearchClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            SearchIcon(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp).size(24.dp)
            )
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background =
        if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .background(background)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/** Lente d'ingrandimento disegnata a mano con Canvas (nessuna dipendenza esterna di icone). */
@Composable
private fun SearchIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val radius = size.minDimension * 0.30f
        val center = Offset(size.width * 0.40f, size.height * 0.40f)

        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = stroke)
        )

        val handleStart = Offset(
            x = center.x + radius * 0.7f,
            y = center.y + radius * 0.7f
        )
        val handleEnd = Offset(x = size.width * 0.85f, y = size.height * 0.85f)
        drawLine(
            color = color,
            start = handleStart,
            end = handleEnd,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
