package com.app.loyal.barcode

class Ean13Encoder : BarcodeEncoder {

    private val lCodes = listOf(
        "0001101", "0011001", "0010011", "0111101", "0100011",
        "0110001", "0101111", "0111011", "0110111", "0001011"
    )

    private val gCodes = listOf(
        "0100111", "0110011", "0011011", "0100001", "0011101",
        "0111001", "0000101", "0010001", "0001001", "0010111"
    )

    private val rCodes = listOf(
        "1110010", "1100110", "1101100", "1000010", "1011100",
        "1001110", "1010000", "1000100", "1001000", "1110100"
    )

    private val parityPatterns = listOf(
        "LLLLLL", "LLGLGG", "LLGGLG", "LLGGGL", "LGLLGG",
        "LGGLLG", "LGGGLL", "LGLGLG", "LGLGGL", "LGGLGL"
    )

    override fun encode(value: String): List<Boolean> {
        val digits = normalize(value)
        val firstDigit = digits[0] - '0'
        val leftDigits = digits.substring(1, 7)
        val rightDigits = digits.substring(7, 13)
        val parity = parityPatterns[firstDigit]

        val bits = StringBuilder()
        bits.append("101")
        leftDigits.forEachIndexed { index, char ->
            val digit = char - '0'
            bits.append(if (parity[index] == 'L') lCodes[digit] else gCodes[digit])
        }
        bits.append("01010")
        rightDigits.forEach { char ->
            bits.append(rCodes[char - '0'])
        }
        bits.append("101")

        return bits.map { it == '1' }
    }

    private fun normalize(value: String): String {
        val digitsOnly = value.filter { it.isDigit() }
        return when (digitsOnly.length) {
            12 -> digitsOnly + checkDigit(digitsOnly)
            13 -> digitsOnly
            else -> throw IllegalArgumentException(
                "EAN-13 code must have 12 or 13 digits, got ${digitsOnly.length}"
            )
        }
    }

    private fun checkDigit(twelveDigits: String): Char {
        val sum = twelveDigits.mapIndexed { index, char ->
            val digit = char - '0'
            if (index % 2 == 0) digit else digit * 3
        }.sum()
        val remainder = sum % 10
        val check = if (remainder == 0) 0 else 10 - remainder
        return '0' + check
    }
}