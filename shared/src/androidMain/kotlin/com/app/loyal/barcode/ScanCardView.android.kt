package com.app.loyal.barcode

import android.widget.Toast
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.app.loyal.model.BarcodeFormat
import com.app.loyal.util.rememberCameraPermissionGranted
import org.ncgroup.kscan.Barcode
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerUiOptions
import org.ncgroup.kscan.ScannerView
import org.ncgroup.kscan.scannerColors
import org.ncgroup.kscan.BarcodeFormat as KScanFormat

private fun Barcode.toAppFormat(): BarcodeFormat? = when (format) {
    "FORMAT_QR_CODE" -> BarcodeFormat.QR_CODE
    "FORMAT_EAN_13" -> BarcodeFormat.EAN_13
    "FORMAT_CODE_128" -> BarcodeFormat.CODE_128
    "FORMAT_ITF" -> BarcodeFormat.ITF
    "FORMAT_UPC_A" -> BarcodeFormat.UPC_A
    else -> null
}

@Composable
actual fun ScanCardView(
    onScanned: (code: String, format: BarcodeFormat) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    if (rememberCameraPermissionGranted()) {
        ScannerView(
            codeTypes = listOf(
                KScanFormat.FORMAT_QR_CODE,
                KScanFormat.FORMAT_EAN_13,
                KScanFormat.FORMAT_CODE_128,
                KScanFormat.FORMAT_ITF,
                KScanFormat.FORMAT_UPC_A
            ),
            colors = scannerColors(
                headerContainerColor = Color.Transparent
            ),
            scannerUiOptions = ScannerUiOptions(headerTitle = "")
        ) { result ->
            when (result) {
                is BarcodeResult.OnSuccess -> {
                    val appFormat = result.barcode.toAppFormat()
                    if (appFormat != null) {
                        onScanned(result.barcode.data, appFormat)
                    } else {
                        Toast.makeText(
                            context,
                            "Formato non gestito: ${result.barcode.format} (${result.barcode.data})",
                            Toast.LENGTH_LONG
                        ).show()
                        onCancel()
                    }
                }
                is BarcodeResult.OnFailed, BarcodeResult.OnCanceled -> onCancel()
            }
        }
    } else {
        Button(onClick = onCancel) { Text("Permesso fotocamera negato") }
    }
}
