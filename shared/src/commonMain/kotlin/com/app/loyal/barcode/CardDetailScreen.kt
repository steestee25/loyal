package com.app.loyal.barcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.loyal.i18n.LocalStrings
import com.app.loyal.model.LoyaltyCard
import com.app.loyal.ui.HeartIcon
import com.app.loyal.ui.MAX_FAVORITE_CARDS
import coil3.compose.AsyncImage

/**
 * [onToggleFavorite] ritorna false quando la carta non può essere aggiunta ai
 * preferiti perché si è già al limite: in quel caso mostriamo il messaggio.
 */
@Composable
fun CardDetailScreen(
    card: LoyaltyCard,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Boolean,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    var menuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: freccia indietro + logo + nome brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onBack)
                        .padding(4.dp)
                ) {
                    BackArrowIcon(
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
                AsyncImage(
                    model = card.logoUrl,
                    contentDescription = card.brandName,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = card.label?.takeIf { it.isNotBlank() } ?: card.brandName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.weight(1f))

                // Menu "tre puntini" a destra
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { menuExpanded = true }
                            .padding(4.dp)
                    ) {
                        MoreVertIcon(
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (card.isFavorite) strings.removeFromFavorites
                                    else strings.addToFavorites
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                if (!onToggleFavorite()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            strings.favoritesLimitReached(MAX_FAVORITE_CARDS)
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                HeartIcon(
                                    filled = card.isFavorite,
                                    color =
                                        if (card.isFavorite) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.edit) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                PencilIcon(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(strings.delete) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                TrashIcon(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Centro: QR/barcode evidente + numero tessera con icona copia
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CardBarcode(code = card.code, format = card.format)

                Spacer(Modifier.height(32.dp))

                CardCodeRow(code = card.code)
            }

            // Nota (es. "Valido su tutti gli acquisti superiori a 10€")
            card.note?.let { note ->
                Text(
                    text = note,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

        }
    }
}

/** Tre puntini verticali (menu) disegnati a mano con Canvas. */
@Composable
private fun MoreVertIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val radius = size.minDimension * 0.09f
        listOf(0.25f, 0.5f, 0.75f).forEach { fraction ->
            drawCircle(
                color = color,
                radius = radius,
                center = Offset(cx, size.height * fraction)
            )
        }
    }
}

/** Matita "modifica" disegnata a mano con Canvas. */
@Composable
private fun PencilIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Corpo della matita (due lati paralleli in diagonale)
        drawLine(color, Offset(w * 0.30f, h * 0.70f), Offset(w * 0.70f, h * 0.30f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.80f), Offset(w * 0.80f, h * 0.40f), stroke, cap = StrokeCap.Round)
        // Estremità in alto (gomma)
        drawLine(color, Offset(w * 0.70f, h * 0.30f), Offset(w * 0.80f, h * 0.40f), stroke, cap = StrokeCap.Round)
        // Punta in basso a sinistra
        drawLine(color, Offset(w * 0.30f, h * 0.70f), Offset(w * 0.22f, h * 0.88f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.80f), Offset(w * 0.22f, h * 0.88f), stroke, cap = StrokeCap.Round)
    }
}

/** Cestino "cancella" disegnato a mano con Canvas. */
@Composable
private fun TrashIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Coperchio
        drawLine(color, Offset(w * 0.20f, h * 0.28f), Offset(w * 0.80f, h * 0.28f), stroke, cap = StrokeCap.Round)
        // Manico
        drawLine(color, Offset(w * 0.40f, h * 0.28f), Offset(w * 0.40f, h * 0.18f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.60f, h * 0.28f), Offset(w * 0.60f, h * 0.18f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.18f), Offset(w * 0.60f, h * 0.18f), stroke, cap = StrokeCap.Round)
        // Corpo (trapezio)
        drawLine(color, Offset(w * 0.28f, h * 0.28f), Offset(w * 0.33f, h * 0.82f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.72f, h * 0.28f), Offset(w * 0.67f, h * 0.82f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.33f, h * 0.82f), Offset(w * 0.67f, h * 0.82f), stroke, cap = StrokeCap.Round)
        // Righe interne
        drawLine(color, Offset(w * 0.43f, h * 0.40f), Offset(w * 0.45f, h * 0.72f), stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.57f, h * 0.40f), Offset(w * 0.55f, h * 0.72f), stroke, cap = StrokeCap.Round)
    }
}

