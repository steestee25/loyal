package com.app.loyal.barcode

import androidx.compose.runtime.Composable
import com.app.loyal.model.BarcodeFormat

@Composable
expect fun ScanCardView(
    onScanned: (code: String, format: BarcodeFormat) -> Unit,
    onCancel: () -> Unit
)
