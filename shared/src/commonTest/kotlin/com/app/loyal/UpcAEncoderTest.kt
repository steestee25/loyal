package com.app.loyal

import com.app.loyal.barcode.Ean13Encoder
import com.app.loyal.barcode.UpcAEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpcAEncoderTest {

    @Test
    fun upcAEqualsEan13WithLeadingZero() {
        // UPC-A a 12 cifre deve produrre lo stesso pattern di un EAN-13 "0" + 12 cifre.
        val upc = "036000291452"
        assertEquals(
            Ean13Encoder().encode("0$upc"),
            UpcAEncoder().encode(upc)
        )
    }

    @Test
    fun acceptsThirteenDigitFormAlreadyPrefixed() {
        val upc = "036000291452"
        assertEquals(
            UpcAEncoder().encode(upc),
            UpcAEncoder().encode("0$upc")
        )
    }

    @Test
    fun ignoresNonDigitCharacters() {
        assertEquals(
            UpcAEncoder().encode("036000291452"),
            UpcAEncoder().encode("0 36000 29145 2")
        )
    }

    @Test
    fun failsOnWrongLength() {
        assertFailsWith<IllegalArgumentException> { UpcAEncoder().encode("12345") }
    }
}
