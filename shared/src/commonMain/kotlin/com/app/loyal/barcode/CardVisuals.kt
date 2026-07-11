package com.app.loyal.barcode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.app.loyal.model.BarcodeFormat

/**
 * Anteprima della tessera: QR o codice a barre a seconda del formato.
 * Riusata nel dettaglio carta e nell'ultimo step di aggiunta carta.
 */
@Composable
internal fun CardBarcode(
    code: String,
    format: BarcodeFormat,
    modifier: Modifier = Modifier
) {
    when (format) {
        BarcodeFormat.QR_CODE -> QrCodeView(value = code)
        BarcodeFormat.EAN_13 -> {
            val modules = remember(code) { Ean13Encoder().encode(code) }
            BarcodeView(modules = modules, modifier = modifier.padding(horizontal = 24.dp))
        }
        BarcodeFormat.CODE_128 -> {
            val modules = remember(code) { Code128Encoder().encode(code) }
            BarcodeView(modules = modules, modifier = modifier)
        }
        BarcodeFormat.ITF -> {
            val modules = remember(code) { ItfEncoder().encode(code) }
            BarcodeView(modules = modules, modifier = modifier.padding(horizontal = 24.dp))
        }
        BarcodeFormat.UPC_A -> {
            val modules = remember(code) { UpcAEncoder().encode(code) }
            BarcodeView(modules = modules, modifier = modifier.padding(horizontal = 24.dp))
        }
    }
}

/** Numero tessera con icona "copia" di fianco. */
@Composable
internal fun CardCodeRow(
    code: String,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.titleMedium
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .clickable { clipboard.setText(AnnotatedString(code)) }
                .padding(6.dp)
        ) {
            CopyIcon(
                color = MaterialTheme.colorScheme.outlineVariant,
                background = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

/** Freccia "indietro" disegnata a mano con Canvas. */
@Composable
internal fun BackArrowIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val midY = size.height / 2f
        val left = size.width * 0.25f
        val right = size.width * 0.80f

        // Asta orizzontale
        drawLine(
            color = color,
            start = Offset(left, midY),
            end = Offset(right, midY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Punta superiore
        drawLine(
            color = color,
            start = Offset(left, midY),
            end = Offset(size.width * 0.45f, size.height * 0.30f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Punta inferiore
        drawLine(
            color = color,
            start = Offset(left, midY),
            end = Offset(size.width * 0.45f, size.height * 0.70f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/** Icona "copia" (due fogli sovrapposti) disegnata a mano con Canvas. */
@Composable
internal fun CopyIcon(
    color: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 1.5.dp.toPx()
        val corner = CornerRadius(2.dp.toPx())
        val sheet = Size(size.width * 0.62f, size.height * 0.62f)

        // Foglio dietro (in alto a sinistra)
        drawRoundRect(
            color = color,
            topLeft = Offset(0f, 0f),
            size = sheet,
            cornerRadius = corner,
            style = Stroke(width = stroke)
        )

        // Foglio davanti (in basso a destra): riempio col colore di sfondo
        // per mascherare le linee dietro, poi disegno il contorno.
        val frontTopLeft = Offset(size.width * 0.38f, size.height * 0.38f)
        drawRoundRect(
            color = background,
            topLeft = frontTopLeft,
            size = sheet,
            cornerRadius = corner
        )
        drawRoundRect(
            color = color,
            topLeft = frontTopLeft,
            size = sheet,
            cornerRadius = corner,
            style = Stroke(width = stroke)
        )
    }
}
