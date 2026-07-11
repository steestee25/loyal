package com.app.loyal

import com.app.loyal.barcode.ItfEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ItfEncoderTest {

    private fun List<Boolean>.toBitString() = joinToString("") { if (it) "1" else "0" }

    @Test
    fun encodesKnownPairWithStartAndStop() {
        // "12": start(1010) + coppia interlacciata 1/2 + stop(11101).
        // narrow = 1 modulo, wide = 3 moduli.
        val expected = "1010" +   // start: N-bar N-space N-bar N-space
            "1110" +              // j0: bar wide(1), space narrow(2)
            "1000" +              // j1: bar narrow(1), space wide(2)
            "10" +                // j2: bar narrow, space narrow
            "10" +                // j3: bar narrow, space narrow
            "111000" +            // j4: bar wide, space wide
            "11101"               // stop: wide-bar narrow-space narrow-bar

        assertEquals(expected, ItfEncoder().encode("12").toBitString())
    }

    @Test
    fun oddNumberOfDigitsIsPaddedWithLeadingZero() {
        // "123" -> "0123": stessa lunghezza di un input di 4 cifre.
        val odd = ItfEncoder().encode("123")
        val padded = ItfEncoder().encode("0123")
        assertEquals(padded, odd)
    }

    @Test
    fun ignoresNonDigitCharacters() {
        assertEquals(
            ItfEncoder().encode("0488040080860"),
            ItfEncoder().encode("0 488 040 080 860")
        )
    }

    @Test
    fun startsBlackAndEndsBlack() {
        val modules = ItfEncoder().encode("0488040080860")
        assertTrue(modules.first(), "deve iniziare con una barra nera")
        assertTrue(modules.last(), "deve finire con una barra nera")
    }

    @Test
    fun failsOnEmptyInput() {
        assertFailsWith<IllegalArgumentException> { ItfEncoder().encode("abc") }
    }
}
