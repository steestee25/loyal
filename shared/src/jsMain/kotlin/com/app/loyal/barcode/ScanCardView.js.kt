package com.app.loyal.barcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.app.loyal.model.BarcodeFormat

@Composable
actual fun ScanCardView(
    onScanned: (code: String, format: BarcodeFormat) -> Unit,
    onCancel: () -> Unit
) {
    var code by remember { mutableStateOf("") }

    Column {
        Text("Scansione fotocamera non disponibile su questa piattaforma: inserisci il codice manualmente.")
        OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Codice carta") })
        Row {
            Button(onClick = { onScanned(code, BarcodeFormat.QR_CODE) }) { Text("QR") }
            Button(onClick = { onScanned(code, BarcodeFormat.EAN_13) }) { Text("EAN-13") }
            Button(onClick = { onScanned(code, BarcodeFormat.CODE_128) }) { Text("Code128") }
        }
        Button(onClick = onCancel) { Text("Annulla") }
    }
}
