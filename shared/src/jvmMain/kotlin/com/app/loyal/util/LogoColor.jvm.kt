package com.app.loyal.util

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image

actual fun averageColorArgb(imageBytes: ByteArray): Long? {
    val image = try {
        Image.makeFromEncoded(imageBytes)
    } catch (e: Throwable) {
        return null
    }

    val bitmap = Bitmap()
    if (!bitmap.allocPixels(image.imageInfo)) return null
    if (!image.readPixels(bitmap)) return null

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
            val pixel = bitmap.getColor(x, y) // Int ARGB
            val alpha = (pixel ushr 24) and 0xFF
            if (alpha >= 128) {
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
    return (0xFFL shl 24) or ((sumR / count) shl 16) or ((sumG / count) shl 8) or (sumB / count)
}
