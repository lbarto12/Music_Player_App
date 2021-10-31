package com.example.musicapp.Util

import android.graphics.Bitmap
import android.graphics.Color

class ColorUtil {

    companion object{
        fun avgBitMapCol(bitmap: Bitmap): Int {
            var red = 0
            var green = 0
            var blue = 0
            var pixelCount = 0

            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    val pColor = bitmap.getPixel(x, y)
                    pixelCount++
                    red += Color.red(pColor)
                    green += Color.green(pColor)
                    blue += Color.green(pColor)
                }
            }

            return Color.rgb(
                red / pixelCount,
                green / pixelCount,
                blue / pixelCount
            )
        }

    }

}