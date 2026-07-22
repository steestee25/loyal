package com.app.loyal.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.loyal.barcode.BackArrowIcon
import com.app.loyal.barcode.CardBarcode
import com.app.loyal.barcode.CardCodeRow
import com.app.loyal.barcode.ScanCardView
import com.app.loyal.data.BrandSearchApi
import com.app.loyal.data.BrandSearchResult
import com.app.loyal.i18n.LocalStrings
import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Passi del processo di aggiunta carta. */
private enum class AddStep { Brand, Scan, Details }

/** Colori condivisi da tutti i textfield del flusso, per garantire lo stesso aspetto ovunque. */
@Composable
private fun sharedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddEditCardScreen(
    brandSearchApi: BrandSearchApi,
    onSave: (LoyaltyCard) -> Unit,
    onCancel: () -> Unit,
    initialCard: LoyaltyCard? = null
) {
    val strings = LocalStrings.current
    val editing = initialCard != null
    // In modifica saltiamo direttamente ai dettagli: brand e codice esistono già.
    var step by remember { mutableStateOf(if (editing) AddStep.Details else AddStep.Brand) }

    // Brand
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<BrandSearchResult>>(emptyList()) }
    var brandName by remember { mutableStateOf(initialCard?.brandName ?: "") }
    var domain by remember { mutableStateOf(initialCard?.domain ?: "") }
    var logoUrl by remember { mutableStateOf(initialCard?.logoUrl) }
    var colorArgb by remember { mutableStateOf(initialCard?.colorArgb ?: 0xFF6200EEL) }

    // Tessera
    var code by remember { mutableStateOf(initialCard?.code ?: "") }
    var format by remember { mutableStateOf(initialCard?.format) }

    // Dettagli
    var label by remember { mutableStateOf(initialCard?.label ?: "") }
    var note by remember { mutableStateOf(initialCard?.note ?: "") }

    val scope = rememberCoroutineScope()

    when (step) {
        // 1) Ricerca brand: menu con logo + nome che compare sotto il campo
        AddStep.Brand -> {
            LaunchedEffect(query) {
                val q = query.trim()
                if (q.length < 2) {
                    results = emptyList()
                } else {
                    delay(300) // piccolo debounce sulla digitazione
                    results = runCatching { brandSearchApi.search(q) }.getOrDefault(emptyList())
                }
            }

            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    BackButton(onBack = onCancel)

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(strings.searchBrand) },
                        singleLine = true,
                        colors = sharedTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(results) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        brandName = result.name ?: result.domain
                                        domain = result.domain
                                        logoUrl = result.icon
                                        result.icon?.let { icon ->
                                            scope.launch {
                                                brandSearchApi.logoColorArgb(icon)?.let { colorArgb = it }
                                            }
                                        }
                                        results = emptyList()
                                        // 2) apertura automatica della scansione
                                        step = AddStep.Scan
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = result.icon,
                                    contentDescription = result.name,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(result.name ?: result.domain)
                            }
                        }
                    }
                }
            }
        }

        // 2) Scansione automatica della tessera
        AddStep.Scan -> ScanCardView(
            onScanned = { scannedCode, scannedFormat ->
                code = scannedCode
                format = scannedFormat
                step = AddStep.Details
            },
            onCancel = { step = AddStep.Brand }
        )

        // 3) Dettagli: header brand + anteprima tessera + numero + label/note
        AddStep.Details -> Scaffold { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: freccia indietro + logo brand + nome brand
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackButton(onBack = { if (editing) onCancel() else step = AddStep.Scan })
                    AsyncImage(
                        model = logoUrl,
                        contentDescription = brandName,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = brandName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Bordo un filino più marcato del solito grigino usato in home,
                // condiviso tra anteprima tessera e card dettagli.
                val emphasizedBorderColor = loyaltyCardBorderColor(colorArgb).let {
                    it.copy(alpha = (it.alpha + 0.15f).coerceAtMost(1f))
                }

                // Anteprima tessera dentro una card bordata, stesso colore della home
                val currentFormat = format
                if (currentFormat != null && code.isNotBlank()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, emphasizedBorderColor),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CardBarcode(code = code, format = currentFormat)
                            Spacer(Modifier.height(16.dp))
                            CardCodeRow(code = code)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Card "dettagli": label e note raggruppate
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, emphasizedBorderColor),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            text = strings.details,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = label,
                            onValueChange = { label = it },
                            label = { Text(strings.cardName) },
                            singleLine = true,
                            colors = sharedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(4.dp))

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text(strings.notes) },
                            colors = sharedTextFieldColors(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val scannedFormat = format ?: return@Button
                        onSave(
                            LoyaltyCard(
                                // UUID e non il timestamp: due dispositivi che
                                // aggiungono una carta nello stesso millisecondo
                                // generavano lo stesso id, e update() fa upsert.
                                id = initialCard?.id ?: Uuid.random().toString(),
                                brandName = brandName,
                                domain = domain,
                                code = code,
                                format = scannedFormat,
                                colorArgb = colorArgb,
                                label = label.ifBlank { null },
                                note = note.ifBlank { null },
                                createdAt = initialCard?.createdAt ?: Clock.System.now(),
                                logoUrl = logoUrl,
                                // Il salvataggio fa un upsert dell'intera riga: senza
                                // riportare questi campi, modificare una carta
                                // azzererebbe preferito e statistiche d'uso.
                                usageCount = initialCard?.usageCount ?: 0,
                                lastViewedAt = initialCard?.lastViewedAt,
                                isFavorite = initialCard?.isFavorite ?: false
                            )
                        )
                    },
                    enabled = brandName.isNotBlank() && domain.isNotBlank() &&
                        code.isNotBlank() && format != null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.save)
                }
            }
        }
    }
}

/** Tasto "indietro" nell'header, stessa UI usata nel dettaglio carta. */
@Composable
private fun BackButton(onBack: () -> Unit) {
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
}
