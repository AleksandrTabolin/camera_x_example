package com.test.camerax.core.camera

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DefaultCameraProvider(
    container: FrameLayout,
    lifecycleOwner: LifecycleOwner
) : CameraProvider {

    private val cameraView = CameraView(container.context)

    init {
        container.addView(
            cameraView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        bindToLifecycle(cameraView, lifecycleOwner)
    }

    @SuppressLint("MissingPermission")
    private fun bindToLifecycle(cameraView: CameraView, lifecycleOwner: LifecycleOwner) {
        cameraView.bindToLifecycle(lifecycleOwner)
    }

    override suspend fun takePicture(): ImageProxy? {
        return suspendCoroutine { continuation ->
            cameraView.takePicture(
                ContextCompat.getMainExecutor(cameraView.context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        continuation.resume(image)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                })
        }
    }

    override fun switchLensFacing() {
        val currentLensFacing = cameraView.cameraLensFacing
        cameraView.cameraLensFacing = when (currentLensFacing) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
            else -> CameraSelector.LENS_FACING_FRONT
        }
    }

    override fun toggleFlash() {
        val nextFlash = when (currentFlash()) {
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_OFF
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            else -> throw IllegalStateException("Unsupport flash")
        }
        cameraView.flash = nextFlash
    }

    override fun toggleTorch() {
        cameraView.enableTorch(!cameraView.isTorchOn)
    }

    override fun currentFlash(): Int = cameraView.flash

    override fun currentTorch(): Boolean = cameraView.isTorchOn
}