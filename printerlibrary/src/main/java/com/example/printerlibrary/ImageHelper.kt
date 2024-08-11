package com.example.printerlibrary

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import kotlin.experimental.or


class ImageHelper(private val context: Context) {


    fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val grayscaleBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayscaleBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f) // Set saturation to 0 for grayscale
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayscaleBitmap
    }


    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val bytesPerRow = (width + 7) / 8 // Calculate the number of bytes per row (8 pixels per byte)

        val data = ByteArray(height * bytesPerRow)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixelColor = bitmap.getPixel(x, y)
                val grayscaleValue = (Color.red(pixelColor) + Color.green(pixelColor) + Color.blue(pixelColor)) / 3
                val bitValue = if (grayscaleValue < 128) 1 else 0

                // Calculate the position in the byte array
                val byteIndex = y * bytesPerRow + x / 8
                val bitIndex = 7 - (x % 8) // 1bpp, most significant bit is on the left

                // Set the bit in the byte
                data[byteIndex] = (data[byteIndex] or ((bitValue shl bitIndex).toByte()))
            }
        }

        return data
    }


    fun textToMultilineBitmap(text: String, fontId: Int, fontSize: Float, maxWidth: Int): Bitmap {
        // Create a TextPaint object with the desired font and size
        val paint = TextPaint()
        paint.color = Color.BLACK
        paint.textSize = fontSize

        // Set the desired font
        val typeface = ResourcesCompat.getFont(context, fontId)
        paint.typeface = Typeface.create(typeface, Typeface.BOLD)

        // Create a StaticLayout to handle multiline text
        val staticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            paint,
            maxWidth
        ).build()

        // Create a bitmap with the calculated dimensions
        val bitmap = Bitmap.createBitmap(maxWidth, staticLayout.height, Bitmap.Config.ARGB_8888)

        // Create a canvas and draw the text on the bitmap
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        staticLayout.draw(canvas)

        return bitmap
    }
}