package com.test.camerax.core.camera

import android.content.ContentResolver
import androidx.camera.core.ImageProxy

interface CameraProvider {

    suspend fun takePicture(): ImageProxy?

    fun switchLensFacing()

    fun toggleFlash()

    fun currentFlash(): Int

    fun currentTorch(): Boolean

    fun toggleTorch()
}