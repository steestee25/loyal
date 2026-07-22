package com.app.loyal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.app.loyal.i18n.AppLanguage
import com.app.loyal.i18n.LocalStrings
import com.app.loyal.model.LoyaltyCard
import coil3.compose.AsyncImage
import io.github.jan.supabase.SupabaseClient

enum class Tab { Home, Settings }

/**
 * Colore del bordo di una tessera a partire dal suo [colorArgb].
 * Colori scuri/saturi (bassa luminosità) risultano troppo marcati: li rendiamo
 * più tenui abbassando l'alpha, mentre i colori chiari restano pieni.
 */
internal fun loyaltyCardBorderColor(colorArgb: Long): Color {
    val baseColor = Color(colorArgb)
    val borderAlpha = (0.16f + 1.7f * baseColor.luminance()).coerceIn(0.16f, 1f)
    return baseColor.copy(alpha = borderAlpha)
}

@Composable
fun CardListScreen(
    viewModel: CardListViewModel,
    userEmail: String,
    supabase: SupabaseClient,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onAddClick: () -> Unit,
    onCardClick: (LoyaltyCard) -> Unit,
    onLogoutClick: () -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    val strings = LocalStrings.current
    val cards by viewModel.cards.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    var selectedTab by remember { mutableStateOf(Tab.Home) }
    var query by remember { mutableStateOf("") }
    var showSortSheet by remember { mutableStateOf(false) }

    val filteredCards = remember(cards, query) {
        val q = query.trim()
        if (q.isEmpty()) cards
        else cards.filter { it.brandName.contains(q, ignoreCase = true) }
    }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                Tab.Home -> Column(modifier = Modifier.fillMaxSize()) {
                    HomeSearchHeader(
                        query = query,
                        onQueryChange = { query = it },
                        onSortClick = { showSortSheet = true },
                        onProfileClick = { selectedTab = Tab.Settings }
                    )
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(filteredCards) { card ->
                            LoyaltyCardItem(
                                card = card,
                                onClick = {
                                    viewModel.recordView(card.id)
                                    onCardClick(card)
                                },
                                onFavoriteClick = { viewModel.toggleFavorite(card) }
                            )
                        }
                    }
                }

                Tab.Settings -> SettingsScreen(
                    userEmail = userEmail,
                    supabase = supabase,
                    language = language,
                    onLanguageChange = onLanguageChange,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            FloatingNavBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onAddClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            )
        }
    }

    if (showSortSheet) {
        SortOrderBottomSheet(
            selected = sortOrder,
            onSelected = {
                viewModel.setSortOrder(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }
}

/**
 * Header della home: textfield rotondeggiante bianca per la ricerca, con l'icona di
 * ordinamento in fondo a destra (apre il bottom sheet) e l'icona profilo a destra (va alle impostazioni).
 */
@Composable
private fun HomeSearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onSortClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(strings.searchPlaceholder) },
            singleLine = true,
            shape = RoundedCornerShape(50),
            trailingIcon = {
                IconButton(onClick = onSortClick) {
                    SortIcon(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        CircleIconButton(onClick = onProfileClick) {
            ProfileIcon(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(10.dp).size(24.dp)
            )
        }
    }
}

/** Bottom sheet con le 3 opzioni di ordinamento (prima erano dei FilterChip). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOrderBottomSheet(
    selected: CardSortOrder,
    onSelected: (CardSortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = strings.sortBy,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 8.dp)
            )
            SortOption(
                label = strings.sortAlphabetical,
                selected = selected == CardSortOrder.Alphabetical,
                onClick = { onSelected(CardSortOrder.Alphabetical) }
            )
            SortOption(
                label = strings.sortMostUsed,
                selected = selected == CardSortOrder.MostUsed,
                onClick = { onSelected(CardSortOrder.MostUsed) }
            )
            SortOption(
                label = strings.sortRecentlyViewed,
                selected = selected == CardSortOrder.RecentlyViewed,
                onClick = { onSelected(CardSortOrder.RecentlyViewed) }
            )
        }
    }
}

@Composable
private fun SortOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(percent = 50)
    val contentColor =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface

    // Quando selezionata, l'opzione è evidenziata da un contenitore a pillola:
    // sfondo tenue + bordo di 1dp del colore primario.
    val containerModifier =
        if (selected) {
            Modifier
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), shape)
                .border(1.dp, MaterialTheme.colorScheme.primary, shape)
        } else {
            Modifier
        }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(shape)
            .then(containerModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
        if (selected) {
            CheckIcon(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
internal fun LoyaltyCardItem(
    card: LoyaltyCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: (() -> Unit)? = null
) {
    val strings = LocalStrings.current
    val borderColor = loyaltyCardBorderColor(card.colorArgb)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(card.brandName)
                Text(card.code)
            }

            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    HeartIcon(
                        filled = card.isFavorite,
                        color =
                            if (card.isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription =
                            if (card.isFavorite) strings.removeFromFavorites
                            else strings.addToFavorites,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FloatingNavBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalStrings.current
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
                    label = strings.navHome,
                    selected = selectedTab == Tab.Home,
                    onClick = { onTabSelected(Tab.Home) }
                )
                NavItem(
                    label = strings.navSettings,
                    selected = selectedTab == Tab.Settings,
                    onClick = { onTabSelected(Tab.Settings) }
                )
            }
        }

        // Bottone "aggiungi carta" staccato
        CircleIconButton(onClick = onAddClick) {
            AddCardIcon(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp).size(34.dp)
            )
        }
    }
}

/** Bottone circolare con ombra, riusato per lente e "aggiungi carta". */
@Composable
private fun CircleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        content()
    }
}

/** Icona "aggiungi carta": una carta (con angolo aperto) e un "+" in basso a destra. */
@Composable
private fun AddCardIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = size.minDimension * 0.06f
        val r = w * 0.11f

        val left = w * 0.08f
        val right = w * 0.70f
        val top = h * 0.10f
        val bottom = h * 0.66f

        val bottomGapX = w * 0.46f
        val rightGapY = h * 0.46f

        // "+" nella nicchia vuota, tra il bordo inferiore e il bordo destro della carta
        val cx = (bottomGapX + right) / 2f
        val cy = (rightGapY + bottom) / 2f
        val pr = w * 0.12f

        // Il glifo non è simmetrico: occupa la zona in alto a sinistra del Canvas.
        // Calcoliamo il suo bounding box e lo ricentriamo con un translate.
        val minX = left
        val maxX = cx + pr
        val minY = top
        val maxY = cy + pr
        val dx = (w - (minX + maxX)) / 2f
        val dy = (h - (minY + maxY)) / 2f

        translate(left = dx, top = dy) {
            // Bordo carta: parte dal bordo inferiore, gira in senso antiorario e
            // si ferma sul bordo destro, lasciando "aperto" l'angolo in basso a destra.
            val cardPath = Path().apply {
                moveTo(bottomGapX, bottom)
                lineTo(left + r, bottom)
                quadraticTo(left, bottom, left, bottom - r)   // angolo in basso a sinistra
                lineTo(left, top + r)
                quadraticTo(left, top, left + r, top)         // angolo in alto a sinistra
                lineTo(right - r, top)
                quadraticTo(right, top, right, top + r)        // angolo in alto a destra
                lineTo(right, rightGapY)
            }
            drawPath(
                path = cardPath,
                color = color,
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Banda magnetica (linea orizzontale sotto il bordo superiore)
            val stripeY = h * 0.28f
            drawLine(color, Offset(left, stripeY), Offset(right, stripeY), strokeWidth = stroke, cap = StrokeCap.Round)

            // Piccolo trattino in basso a sinistra
            val dashY = h * 0.45f
            drawLine(color, Offset(w * 0.16f, dashY), Offset(w * 0.27f, dashY), strokeWidth = stroke, cap = StrokeCap.Round)

            drawLine(color, Offset(cx - pr, cy), Offset(cx + pr, cy), strokeWidth = stroke, cap = StrokeCap.Round)
            drawLine(color, Offset(cx, cy - pr), Offset(cx, cy + pr), strokeWidth = stroke, cap = StrokeCap.Round)
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

/** Icona di ordinamento: due frecce verticali opposte (su/giù), disegnata a mano con Canvas. */
@Composable
private fun SortIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 1.8.dp.toPx()
        val w = size.width
        val h = size.height
        val head = w * 0.16f

        // Freccia sinistra: punta in alto
        val xUp = w * 0.32f
        drawLine(color, Offset(xUp, h * 0.14f), Offset(xUp, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(xUp, h * 0.14f), Offset(xUp - head, h * 0.36f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(xUp, h * 0.14f), Offset(xUp + head, h * 0.36f), strokeWidth = stroke, cap = StrokeCap.Round)

        // Freccia destra: punta in basso
        val xDown = w * 0.68f
        drawLine(color, Offset(xDown, h * 0.14f), Offset(xDown, h * 0.86f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(xDown, h * 0.86f), Offset(xDown - head, h * 0.64f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(xDown, h * 0.86f), Offset(xDown + head, h * 0.64f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

/** Icona profilo: testa (cerchio) e spalle (arco), disegnata a mano con Canvas. */
@Composable
internal fun ProfileIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height

        val headRadius = w * 0.18f
        drawCircle(
            color = color,
            radius = headRadius,
            center = Offset(w * 0.5f, h * 0.32f),
            style = Stroke(width = stroke)
        )

        val shoulderRadius = w * 0.34f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.5f - shoulderRadius, h * 0.60f),
            size = Size(shoulderRadius * 2, shoulderRadius * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/**
 * Cuore dei preferiti, disegnato a mano con Canvas.
 * [filled] = preferito (cuore pieno), altrimenti solo il contorno.
 */
@Composable
internal fun HeartIcon(
    filled: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    Canvas(
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
        }
    ) {
        val w = size.width
        val h = size.height

        // Due archi in alto che si incontrano al centro, poi i due fianchi che
        // scendono a punta sul fondo.
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.88f)
            cubicTo(w * 0.5f, h * 0.88f, w * 0.04f, h * 0.56f, w * 0.04f, h * 0.34f)
            cubicTo(w * 0.04f, h * 0.10f, w * 0.36f, h * 0.04f, w * 0.5f, h * 0.28f)
            cubicTo(w * 0.64f, h * 0.04f, w * 0.96f, h * 0.10f, w * 0.96f, h * 0.34f)
            cubicTo(w * 0.96f, h * 0.56f, w * 0.5f, h * 0.88f, w * 0.5f, h * 0.88f)
            close()
        }

        if (filled) {
            drawPath(path = path, color = color)
        } else {
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

/** Spunta di conferma, disegnata a mano con Canvas. */
@Composable
private fun CheckIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(w * 0.15f, h * 0.55f), Offset(w * 0.42f, h * 0.80f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.42f, h * 0.80f), Offset(w * 0.85f, h * 0.25f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
