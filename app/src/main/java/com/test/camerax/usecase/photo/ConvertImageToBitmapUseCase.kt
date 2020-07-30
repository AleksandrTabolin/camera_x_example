package com.test.camerax.usecase.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

class ConvertImageToBitmapUseCase {

    fun convertToBitmap(image: ImageProxy): Bitmap {
        return when (image.format) {
            ImageFormat.JPEG -> convertJpegImageToBitmap(image)
            else -> throw IllegalStateException("can't convert ${getImageFormatLabel(image)}")
        }
    }

    private fun getImageFormatLabel(image: ImageProxy): String {
        return when (image.format) {
            ImageFormat.UNKNOWN -> "UNKNOWN"
            ImageFormat.RGB_565 -> "RGB_565"
            ImageFormat.YV12 -> "YV12"
            ImageFormat.Y8 -> "Y8"
            ImageFormat.NV16 -> "NV16"
            ImageFormat.NV21 -> "NV21"
            ImageFormat.YUY2 -> "YUY2"
            ImageFormat.JPEG -> "JPEG"
            ImageFormat.DEPTH_JPEG -> "DEPTH_JPEG"
            ImageFormat.YUV_420_888 -> "YUV_420_888"
            ImageFormat.YUV_422_888 -> "YUV_422_888"
            ImageFormat.YUV_444_888 -> "YUV_444_888"
            ImageFormat.FLEX_RGB_888 -> "FLEX_RGB_888"
            ImageFormat.FLEX_RGBA_8888 -> "FLEX_RGBA_8888"
            ImageFormat.RAW_SENSOR -> "RAW_SENSOR"
            ImageFormat.RAW_PRIVATE -> "RAW_PRIVATE"
            ImageFormat.RAW10 -> "RAW10"
            ImageFormat.RAW12 -> "RAW12"
            ImageFormat.DEPTH16 -> "DEPTH16"
            ImageFormat.DEPTH_POINT_CLOUD -> "DEPTH_POINT_CLOUD"
            ImageFormat.PRIVATE -> "PRIVATE"
            ImageFormat.HEIC -> "HEIC"
            else -> "UNKNOWN<${image.format}>"
        }
    }

    private fun convertJpegImageToBitmap(image: ImageProxy): Bitmap {
        if (image.format != ImageFormat.JPEG) throw IllegalStateException("can't convert not JPEG to bitmap")

        val bytes = toByteArray(image.planes[0].buffer)

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
    }

    private fun toByteArray(buffer: ByteBuffer): ByteArray {
        return ByteArray(buffer.capacity()).apply {
            buffer.get(this)
        }
    }

}