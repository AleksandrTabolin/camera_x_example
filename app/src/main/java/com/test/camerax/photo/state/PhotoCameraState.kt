package com.test.camerax.photo.state

data class PhotoCameraState(
    val enableInput: Boolean = true
) {
    companion object {
        val INIT = PhotoCameraState()
    }
}