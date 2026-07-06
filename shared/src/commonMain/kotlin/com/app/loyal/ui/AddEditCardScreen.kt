package com.app.loyal.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import kotlin.time.Clock

@Composable
fun AddEditCardScreen(
    onSave: (LoyaltyCard) -> Unit,
    onCancel: () -> Unit
) {
    var brandName by remember { mutableStateOf("") }
    var domain by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = brandName,
                onValueChange = { brandName = it },
                label = { Text("Nome brand") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                label = { Text("Dominio (es. starbucks.com)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Codice carta") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                onSave(
                    LoyaltyCard(
                        id = Clock.System.now().toEpochMilliseconds().toString(),
                        brandName = brandName,
                        domain = domain,
                        code = code,
                        format = BarcodeFormat.QR_CODE,
                        colorArgb = 0xFF6200EE,
                        createdAt = Clock.System.now()
                    )
                )
            }) {
                Text("Salva")
            }
            Button(onClick = onCancel) {
                Text("Annulla")
            }
        }
    }
}