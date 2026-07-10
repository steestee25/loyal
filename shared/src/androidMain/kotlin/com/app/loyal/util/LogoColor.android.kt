package com.app.loyal.util

import android.graphics.BitmapFactory

actual fun averageColorArgb(imageBytes: ByteArray): Long? {
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return null

    var sumR = 0L
    var sumG = 0L
    var sumB = 0L
    var count = 0L

    val stepX = maxOf(1, bitmap.width / 32)
    val stepY = maxOf(1, bitmap.height / 32)

    var y = 0
    while (y < bitmap.height) {
        var x = 0
        while (x < bitmap.width) {
            val pixel = bitmap.getPixel(x, y)
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha >= 128) { // ignora i pixel (semi)trasparenti
                sumR += (pixel ushr 16) and 0xFF
                sumG += (pixel ushr 8) and 0xFF
                sumB += pixel and 0xFF
                count++
            }
            x += stepX
        }
        y += stepY
    }

    if (count == 0L) return null
    return argb(sumR / count, sumG / count, sumB / count)
}

private fun argb(r: Long, g: Long, b: Long): Long =
    (0xFFL shl 24) or (r shl 16) or (g shl 8) or b
