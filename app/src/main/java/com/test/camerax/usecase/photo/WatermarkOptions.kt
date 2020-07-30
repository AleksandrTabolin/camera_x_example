package com.test.camerax.usecase.photo

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import androidx.annotation.ColorInt

data class WatermarkOptions(
    val corner: AddWatermarkUseCase.Corner = AddWatermarkUseCase.Corner.BOTTOM_RIGHT,
    val textSizeToWidthRatio: Float = 0.04f,
    val paddingToWidthRatio: Float = 0.03f,
    @ColorInt val textColor: Int = Color.WHITE,
    @ColorInt val shadowColor: Int? = Color.BLACK,
    val typeface: Typeface? = null,
    val rotation: Int = 0
)