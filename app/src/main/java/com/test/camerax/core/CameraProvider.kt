package com.test.camerax.core

import android.content.ContentResolver
import androidx.camera.core.ImageProxy

interface CameraProvider {

    suspend fun takePicture(): ImageProxy?

    fun provideContentResolver(): ContentResolver

    fun switchLensFacing()

}