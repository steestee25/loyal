package com.app.loyal.barcode

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.app.loyal.model.BarcodeFormat
import com.app.loyal.util.rememberCameraPermissionGranted
import org.ncgroup.kscan.Barcode
import org.ncgroup.kscan.BarcodeResult
import org.ncgroup.kscan.ScannerView
import org.ncgroup.kscan.BarcodeFormat as KScanFormat

private fun Barcode.toAppFormat(): BarcodeFormat? = when (format) {
    "FORMAT_QR_CODE" -> BarcodeFormat.QR_CODE
    "FORMAT_EAN_13" -> BarcodeFormat.EAN_13
    "FORMAT_CODE_128" -> BarcodeFormat.CODE_128
    else -> null
}

@Composable
actual fun ScanCardView(
    onScanned: (code: String, format: BarcodeFormat) -> Unit,
    onCancel: () -> Unit
) {
    if (rememberCameraPermissionGranted()) {
        ScannerView(
            codeTypes = listOf(KScanFormat.FORMAT_QR_CODE, KScanFormat.FORMAT_EAN_13, KScanFormat.FORMAT_CODE_128)
        ) { result ->
            when (result) {
                is BarcodeResult.OnSuccess -> result.barcode.toAppFormat()?.let { onScanned(result.barcode.data, it) }
                is BarcodeResult.OnFailed, BarcodeResult.OnCanceled -> onCancel()
            }
        }
    } else {
        Button(onClick = onCancel) { Text("Permesso fotocamera negato") }
    }
}
