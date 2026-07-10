package com.app.loyal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.loyal.barcode.ScanCardView
import com.app.loyal.data.BrandSearchApi
import com.app.loyal.data.BrandSearchResult
import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import kotlinx.coroutines.launch
import kotlin.time.Clock

@Composable
fun AddEditCardScreen(
    brandSearchApi: BrandSearchApi,
    onSave: (LoyaltyCard) -> Unit,
    onCancel: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<BrandSearchResult>>(emptyList()) }
    var brandName by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var format by remember { mutableStateOf<BarcodeFormat?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var logoUrl by remember { mutableStateOf<String?>(null) }

    if (showScanner) {
        ScanCardView(
            onScanned = { scannedCode, scannedFormat ->
                code = scannedCode
                format = scannedFormat
                showScanner = false
            },
            onCancel = { showScanner = false }
        )
        return
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Cerca brand") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                scope.launch { results = brandSearchApi.search(query) }
            }) {
                Text("Cerca")
            }

            LazyColumn {
                items(results) { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            brandName = result.name ?: result.domain
                            domain = result.domain
                            logoUrl = result.icon
                            results = emptyList()
                            query = result.name ?: result.domain
                        }.padding(8.dp)
                    ) {
                        Text("${result.name ?: result.domain} (${result.domain})")
                    }
                }
            }

            Text("Selezionato: $brandName / $domain")

            Button(onClick = { showScanner = true }) {
                Text("Scansiona tessera")
            }
            if (code.isNotBlank() && format != null) {
                Text("Codice: $code (${format!!.name})")
            }
            Button(
                onClick = {
                    val scannedFormat = format ?: return@Button
                    onSave(
                        LoyaltyCard(
                            id = Clock.System.now().toEpochMilliseconds().toString(),
                            brandName = brandName,
                            domain = domain,
                            code = code,
                            format = scannedFormat,
                            colorArgb = 0xFF6200EE,
                            createdAt = Clock.System.now(),
                            logoUrl = logoUrl
                        )
                    )
                },
                enabled = brandName.isNotBlank() && domain.isNotBlank() && code.isNotBlank() && format != null
            ) {
                Text("Salva")
            }
            Button(onClick = onCancel) {
                Text("Annulla")
            }
        }
    }
}