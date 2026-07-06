package com.app.loyal.barcode

class Code128Encoder : BarcodeEncoder {

    private val patterns = listOf(
        listOf(2, 1, 2, 2, 2, 2), listOf(2, 2, 2, 1, 2, 2), listOf(2, 2, 2, 2, 2, 1),
        listOf(1, 2, 1, 2, 2, 3), listOf(1, 2, 1, 3, 2, 2), listOf(1, 3, 1, 2, 2, 2),
        listOf(1, 2, 2, 2, 1, 3), listOf(1, 2, 2, 3, 1, 2), listOf(1, 3, 2, 2, 1, 2),
        listOf(2, 2, 1, 2, 1, 3), listOf(2, 2, 1, 3, 1, 2), listOf(2, 3, 1, 2, 1, 2),
        listOf(1, 1, 2, 2, 3, 2), listOf(1, 2, 2, 1, 3, 2), listOf(1, 2, 2, 2, 3, 1),
        listOf(1, 1, 3, 2, 2, 2), listOf(1, 2, 3, 1, 2, 2), listOf(1, 2, 3, 2, 2, 1),
        listOf(2, 2, 3, 2, 1, 1), listOf(2, 2, 1, 1, 3, 2), listOf(2, 2, 1, 2, 3, 1),
        listOf(2, 1, 3, 2, 1, 2), listOf(2, 2, 3, 1, 1, 2), listOf(3, 1, 2, 1, 3, 1),
        listOf(3, 1, 1, 2, 2, 2), listOf(3, 2, 1, 1, 2, 2), listOf(3, 2, 1, 2, 2, 1),
        listOf(3, 1, 2, 2, 1, 2), listOf(3, 2, 2, 1, 1, 2), listOf(3, 2, 2, 2, 1, 1),
        listOf(2, 1, 2, 1, 2, 3), listOf(2, 1, 2, 3, 2, 1), listOf(2, 3, 2, 1, 2, 1),
        listOf(1, 1, 1, 3, 2, 3), listOf(1, 3, 1, 1, 2, 3), listOf(1, 3, 1, 3, 2, 1),
        listOf(1, 1, 2, 3, 1, 3), listOf(1, 3, 2, 1, 1, 3), listOf(1, 3, 2, 3, 1, 1),
        listOf(2, 1, 1, 3, 1, 3), listOf(2, 3, 1, 1, 1, 3), listOf(2, 3, 1, 3, 1, 1),
        listOf(1, 1, 2, 1, 3, 3), listOf(1, 1, 2, 3, 3, 1), listOf(1, 3, 2, 1, 3, 1),
        listOf(1, 1, 3, 1, 2, 3), listOf(1, 1, 3, 3, 2, 1), listOf(1, 3, 3, 1, 2, 1),
        listOf(3, 1, 3, 1, 2, 1), listOf(2, 1, 1, 3, 3, 1), listOf(2, 3, 1, 1, 3, 1),
        listOf(2, 1, 3, 1, 1, 3), listOf(2, 1, 3, 3, 1, 1), listOf(2, 1, 3, 1, 3, 1),
        listOf(3, 1, 1, 1, 2, 3), listOf(3, 1, 1, 3, 2, 1), listOf(3, 3, 1, 1, 2, 1),
        listOf(3, 1, 2, 1, 1, 3), listOf(3, 1, 2, 3, 1, 1), listOf(3, 3, 2, 1, 1, 1),
        listOf(3, 1, 4, 1, 1, 1), listOf(2, 2, 1, 4, 1, 1), listOf(4, 3, 1, 1, 1, 1),
        listOf(1, 1, 1, 2, 2, 4), listOf(1, 1, 1, 4, 2, 2), listOf(1, 2, 1, 1, 2, 4),
        listOf(1, 2, 1, 4, 2, 1), listOf(1, 4, 1, 1, 2, 2), listOf(1, 4, 1, 2, 2, 1),
        listOf(1, 1, 2, 2, 1, 4), listOf(1, 1, 2, 4, 1, 2), listOf(1, 2, 2, 1, 1, 4),
        listOf(1, 2, 2, 4, 1, 1), listOf(1, 4, 2, 1, 1, 2), listOf(1, 4, 2, 2, 1, 1),
        listOf(2, 4, 1, 2, 1, 1), listOf(2, 2, 1, 1, 1, 4), listOf(4, 1, 3, 1, 1, 1),
        listOf(2, 4, 1, 1, 1, 2), listOf(1, 3, 4, 1, 1, 1), listOf(1, 1, 1, 2, 4, 2),
        listOf(1, 2, 1, 1, 4, 2), listOf(1, 2, 1, 2, 4, 1), listOf(1, 1, 4, 2, 1, 2),
        listOf(1, 2, 4, 1, 1, 2), listOf(1, 2, 4, 2, 1, 1), listOf(4, 1, 1, 2, 1, 2),
        listOf(4, 2, 1, 1, 1, 2), listOf(4, 2, 1, 2, 1, 1), listOf(2, 1, 2, 1, 4, 1),
        listOf(2, 1, 4, 1, 2, 1), listOf(4, 1, 2, 1, 2, 1), listOf(1, 1, 1, 1, 4, 3),
        listOf(1, 1, 1, 3, 4, 1), listOf(1, 3, 1, 1, 4, 1), listOf(1, 1, 4, 1, 1, 3),
        listOf(1, 1, 4, 3, 1, 1), listOf(4, 1, 1, 1, 1, 3), listOf(4, 1, 1, 3, 1, 1),
        listOf(1, 1, 3, 1, 4, 1), listOf(1, 1, 4, 1, 3, 1), listOf(3, 1, 1, 1, 4, 1),
        listOf(4, 1, 1, 1, 3, 1), listOf(2, 1, 1, 4, 1, 2), listOf(2, 1, 1, 2, 1, 4),
        listOf(2, 1, 1, 2, 3, 2), listOf(2, 3, 3, 1, 1, 1, 2)
    )

    private val startCodeB = 104
    private val stopCode = 106

    override fun encode(value: String): List<Boolean> {
        require(value.all { it.code in 32..126 }) {
            "Code128 (Set B) supports only ASCII characters 32-126"
        }

        val dataSymbols = value.map { it.code - 32 }
        val checksum = (startCodeB + dataSymbols.mapIndexed { index, symbol ->
            symbol * (index + 1)
        }.sum()) % 103

        val symbols = listOf(startCodeB) + dataSymbols + listOf(checksum, stopCode)

        val modules = mutableListOf<Boolean>()
        symbols.forEach { symbol ->
            var isBar = true
            patterns[symbol].forEach { width ->
                repeat(width) { modules.add(isBar) }
                isBar = !isBar
            }
        }
        return modules
    }
}