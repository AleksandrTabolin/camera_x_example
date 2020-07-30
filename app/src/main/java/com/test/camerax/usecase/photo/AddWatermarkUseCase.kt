package com.test.camerax.usecase.photo

import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.DITHER_FLAG

/**
 * https://medium.com/@guygriv/adding-a-text-watermark-to-a-bitmap-f3af0974c430
 */
class AddWatermarkUseCase {

    fun addWatermark(
        bitmap: Bitmap,
        text: String,
        options: WatermarkOptions = WatermarkOptions()
    ): Bitmap {
        val result = bitmap.copy(bitmap.config, true)
        val canvas = Canvas(result)
        val paint = Paint(ANTI_ALIAS_FLAG or DITHER_FLAG)

        paint.textAlign = when (options.corner) {
            Corner.TOP_LEFT,
            Corner.BOTTOM_LEFT -> Paint.Align.LEFT
            Corner.TOP_RIGHT,
            Corner.BOTTOM_RIGHT -> Paint.Align.RIGHT
        }

        paint.color = options.textColor

        val textSize = result.width * options.textSizeToWidthRatio
        paint.textSize = textSize

        if (options.shadowColor != null) {
            paint.setShadowLayer(textSize / 2, 0f, 0f, options.shadowColor)
        }

        if (options.typeface != null) {
            paint.typeface = options.typeface
        }

        val padding = result.width * options.paddingToWidthRatio
        val coordinates = calculateCoordinates(
            text,
            paint,
            options,
            canvas.width,
            canvas.height,
            padding
        )
        canvas.drawText(text, coordinates.x, coordinates.y, paint)
        return result
    }

    private fun calculateCoordinates(
        watermarkText: String,
        paint: Paint,
        options: WatermarkOptions,
        width: Int,
        height: Int,
        padding: Float
    ): PointF {
        val x = when (options.corner) {
            Corner.TOP_LEFT,
            Corner.BOTTOM_LEFT -> padding
            Corner.TOP_RIGHT,
            Corner.BOTTOM_RIGHT -> width - padding
        }

        val y = when (options.corner) {
            Corner.BOTTOM_LEFT,
            Corner.BOTTOM_RIGHT -> height - padding
            Corner.TOP_LEFT,
            Corner.TOP_RIGHT -> paint.calculateTextHeight(watermarkText) + padding
        }

        return PointF(x, y)
    }

    private fun Paint.calculateTextHeight(text: String): Int {
        return with(Rect()) {
            getTextBounds(text, 0, text.length, this)
            height()
        }
    }

    enum class Corner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
    }

}