package com.app.loyal.barcode

/**
 * Encoder per il formato ITF (Interleaved 2 of 5).
 *
 * Codifica cifre a coppie: la prima cifra della coppia va nelle barre nere,
 * la seconda negli spazi bianchi, interlacciate. Ogni cifra ha un pattern di
 * 5 elementi (2 larghi + 3 stretti).
 */
class ItfEncoder : BarcodeEncoder {

    // Per ogni cifra 0-9: true = elemento largo, false = stretto.
    private val patterns = listOf(
        booleanArrayOf(false, false, true, true, false),  // 0
        booleanArrayOf(true, false, false, false, true),  // 1
        booleanArrayOf(false, true, false, false, true),  // 2
        booleanArrayOf(true, true, false, false, false),  // 3
        booleanArrayOf(false, false, true, false, true),  // 4
        booleanArrayOf(true, false, true, false, false),  // 5
        booleanArrayOf(false, true, true, false, false),  // 6
        booleanArrayOf(false, false, false, true, true),  // 7
        booleanArrayOf(true, false, false, true, false),  // 8
        booleanArrayOf(false, true, false, true, false)   // 9
    )

    private val narrow = 1
    private val wide = 3

    override fun encode(value: String): List<Boolean> {
        val digits = normalize(value)
        val modules = mutableListOf<Boolean>()

        // Start: barra-spazio-barra-spazio, tutti stretti.
        addElement(modules, black = true, wide = false)
        addElement(modules, black = false, wide = false)
        addElement(modules, black = true, wide = false)
        addElement(modules, black = false, wide = false)

        // Coppie interlacciate: barre = 1ª cifra, spazi = 2ª cifra.
        var i = 0
        while (i < digits.length) {
            val barPattern = patterns[digits[i] - '0']
            val spacePattern = patterns[digits[i + 1] - '0']
            for (j in 0 until 5) {
                addElement(modules, black = true, wide = barPattern[j])
                addElement(modules, black = false, wide = spacePattern[j])
            }
            i += 2
        }

        // Stop: barra larga, spazio stretto, barra stretta.
        addElement(modules, black = true, wide = true)
        addElement(modules, black = false, wide = false)
        addElement(modules, black = true, wide = false)

        return modules
    }

    private fun addElement(modules: MutableList<Boolean>, black: Boolean, wide: Boolean) {
        val width = if (wide) this.wide else narrow
        repeat(width) { modules.add(black) }
    }

    private fun normalize(value: String): String {
        val digitsOnly = value.filter { it.isDigit() }
        require(digitsOnly.isNotEmpty()) { "ITF code must contain digits" }
        // ITF codifica coppie: se il numero di cifre è dispari, anteponi uno zero.
        return if (digitsOnly.length % 2 == 0) digitsOnly else "0$digitsOnly"
    }
}
