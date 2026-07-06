package com.app.loyal.barcode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun QrCodeView(
    value: String,
    modifier: Modifier = Modifier
) {
    Image(
        painter = rememberQrCodePainter(value),
        contentDescription = null,
        modifier = modifier.size(200.dp)
    )
}
