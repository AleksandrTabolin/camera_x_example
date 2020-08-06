package com.test.camerax.photo.state

import androidx.camera.core.ImageCapture

data class PhotoCameraState(
    val enableInput: Boolean = true,
    val torchState: Boolean = false,
    val flashState: Int = ImageCapture.FLASH_MODE_OFF
) {
    companion object {
        val INIT = PhotoCameraState()
    }
}