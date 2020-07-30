package com.test.camerax.usecase.photo

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SavePhotoUseCase {

    companion object {
        private const val TEMP_FILE_PREFIX = "CameraX"
        private const val TEMP_FILE_SUFFIX = ".tmp"
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    private val addWatermarkUseCase = AddWatermarkUseCase()

    private val convertImageToBitmapUseCase = ConvertImageToBitmapUseCase()

    private val addExifDataUseCase = AddExifDataUseCase()

    private val addToMediaStoreUseCase = AddToMediaStoreUseCase()

    suspend fun savePhoto(image: ImageProxy, contentResolver: ContentResolver) : Uri? {
        return withContext(Dispatchers.IO) { savePhotoInternal(image, contentResolver) }
    }

    private fun savePhotoInternal(image: ImageProxy, contentResolver: ContentResolver) : Uri? {
        val bitmap = convertImageToBitmapUseCase.convertToBitmap(image)

        val timestamp = dateFormat.format(System.currentTimeMillis())

        val bitmapWithWatermark = addWatermarkUseCase.addWatermark(
            bitmap = bitmap,
            text = timestamp,
            options = WatermarkOptions(rotation = image.imageInfo.rotationDegrees)
        )

        val tempFile = File.createTempFile(TEMP_FILE_PREFIX, TEMP_FILE_SUFFIX)
        try {
            FileOutputStream(tempFile).use { out ->
                bitmapWithWatermark.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            addExifDataUseCase.attachExif(tempFile, image)

            return addToMediaStoreUseCase.addPhoto("camera_x_test.JPG", tempFile, contentResolver)
        } finally {
            image.close()
            tempFile.delete()
        }
    }
}



