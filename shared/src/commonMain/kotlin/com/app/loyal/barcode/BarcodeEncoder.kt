package com.app.loyal.barcode

interface BarcodeEncoder {
    fun encode(value: String): List<Boolean>
}