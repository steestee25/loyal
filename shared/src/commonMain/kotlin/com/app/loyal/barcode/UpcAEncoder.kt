package com.app.loyal.barcode

/**
 * Encoder per il formato UPC-A (12 cifre).
 *
 * UPC-A è, a livello di codifica, un EAN-13 con uno "0" iniziale: il pattern
 * dei moduli è identico. Riusiamo quindi [Ean13Encoder] anteponendo lo "0".
 */
class UpcAEncoder : BarcodeEncoder {

    private val ean13 = Ean13Encoder()

    override fun encode(value: String): List<Boolean> {
        val digits = value.filter { it.isDigit() }
        val ean13Value = when (digits.length) {
            12 -> "0$digits"   // UPC-A → EAN-13 con "0" davanti
            13 -> digits       // già in forma EAN-13 (0 + 12 cifre UPC-A)
            else -> throw IllegalArgumentException(
                "UPC-A code must have 12 digits, got ${digits.length}"
            )
        }
        return ean13.encode(ean13Value)
    }
}
