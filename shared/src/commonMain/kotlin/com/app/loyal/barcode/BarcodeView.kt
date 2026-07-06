package com.app.loyal.barcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeView(
    modules: List<Boolean>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        drawRect(color = Color.White)

        val moduleWidth = size.width / modules.size
        modules.forEachIndexed { index, isBlack ->
            if (isBlack) {
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(index * moduleWidth, 0f),
                    size = Size(moduleWidth, size.height)
                )
            }
        }
    }
}