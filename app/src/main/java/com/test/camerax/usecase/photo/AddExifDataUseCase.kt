package com.test.camerax.usecase.photo

import androidx.camera.core.ImageProxy
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AddExifDataUseCase() {

    companion object {
        private val DATETIME_FORMAT = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
    }

    fun attachExif(
        file: File,
        image: ImageProxy
    ) {
        val exif = ExifInterface(file)
        exif.rotate(image.imageInfo.rotationDegrees)
        attachTimestamp(exif)
    }


    private fun attachTimestamp(exif: ExifInterface) {
        val now = System.currentTimeMillis()
        val datetime = convertToExifDateTime(now)

        exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, datetime);
        exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, datetime);

        try {
            val subsec = (now - convertFromExifDateTime(datetime).time).toString()
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL, subsec)
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED, subsec)
        } catch (e: ParseException) {
        }

        exif.saveAttributes()
    }

    private fun convertToExifDateTime(timestamp: Long): String {
        return DATETIME_FORMAT.format(Date(timestamp))
    }

    private fun convertFromExifDateTime(dateTime: String): Date {
        return DATETIME_FORMAT.parse(dateTime)!!
    }
}