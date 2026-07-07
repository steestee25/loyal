package com.app.loyal.barcode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.loyal.barcode.BarcodeView
import com.app.loyal.barcode.Code128Encoder
import com.app.loyal.barcode.Ean13Encoder
import com.app.loyal.barcode.QrCodeView
import com.app.loyal.model.BarcodeFormat
import com.app.loyal.model.LoyaltyCard
import coil3.compose.AsyncImage
import com.app.loyal.data.brandLogoUrl

@Composable
fun CardDetailScreen(
    card: LoyaltyCard,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            AsyncImage(
                model = card.logoUrl,
                contentDescription = card.brandName,
                modifier = Modifier.size(40.dp)
            )
            Text(card.brandName)

            when (card.format) {
                BarcodeFormat.QR_CODE -> QrCodeView(value = card.code)
                BarcodeFormat.EAN_13 -> {
                    val modules = remember(card.code) { Ean13Encoder().encode(card.code) }
                    BarcodeView(modules = modules)
                }
                BarcodeFormat.CODE_128 -> {
                    val modules = remember(card.code) { Code128Encoder().encode(card.code) }
                    BarcodeView(modules = modules)
                }
            }

            Button(onClick = onDelete) { Text("Elimina") }
            Button(onClick = onBack) { Text("Indietro") }
        }
    }
}