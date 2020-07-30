package com.test.camerax.usecase.photo

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException

class AddToMediaStoreUseCase {
    companion object {
        private const val PENDING = 1
        private const val NOT_PENDING = 0
    }

    fun addPhoto(photoName: String, photoFile: File, contentResolver: ContentResolver) : Uri {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, photoName)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativeLocation = Environment.DIRECTORY_DOWNLOADS + File.separator + photoName;
            values.put(MediaStore.Images.ImageColumns.RELATIVE_PATH, relativeLocation)
        }

        setContentValuePending(values, PENDING)

        val outputUri: Uri = contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to create new MediaStore record.")

        copyTempFileToUri(contentResolver, photoFile, outputUri)
        setUriNotPending(values, contentResolver, outputUri)

        return outputUri
    }

    private fun setUriNotPending(
        values: ContentValues,
        contentResolver: ContentResolver,
        outputUri: Uri
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            setContentValuePending(values, NOT_PENDING)
            contentResolver.update(outputUri, values, null, null)
        }
    }

    private fun setContentValuePending(values: ContentValues, isPending: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.IS_PENDING, isPending)
        }
    }

    private fun copyTempFileToUri(
        contentResolver: ContentResolver,
        tempFile: File,
        uri: Uri
    ) {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            tempFile.inputStream().use { inputStream -> inputStream.copyTo(outputStream) }

        }
    }
}