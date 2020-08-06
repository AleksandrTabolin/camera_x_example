package com.test.camerax.core.camera

import android.content.Context
import android.widget.FrameLayout
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.test.camerax.core.getProcessCameraProvider
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class CustomCameraProvider(
    container: FrameLayout,
    private val lifecycleOwner: LifecycleOwner
) : CameraProvider {

    private val context: Context = container.context

    private val previewView = PreviewView(context)

    private lateinit var camera: Camera

    private var cameraLensFacing = CameraSelector.LENS_FACING_BACK
    private var currentFlashMode = ImageCapture.FLASH_MODE_OFF
    private var currentTorch: Boolean = false

    private lateinit var preview: Preview

    private lateinit var imageCapture: ImageCapture

    private lateinit var cameraSelector: CameraSelector

    init {
        container.addView(
            previewView,
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        bindCamera()
    }

    private fun bindCamera() {
        context.getProcessCameraProvider { cameraProvider ->
            preview = Preview.Builder()
                .build()

            imageCapture = ImageCapture.Builder()
                .setFlashMode(currentFlashMode)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()

            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(cameraLensFacing)
                .build()

            cameraProvider .unbindAll()

            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            preview.setSurfaceProvider(previewView.createSurfaceProvider())
        }
    }

    override suspend fun takePicture(): ImageProxy? {
        return suspendCoroutine { continuation ->
            imageCapture.takePicture(
                ContextCompat.getMainExecutor(previewView.context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        if (currentTorch) {
                            currentTorch = false
                            camera.cameraControl.enableTorch(currentTorch)
                        }
                        continuation.resume(image)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                })
        }
    }

    override fun switchLensFacing() {
        val currentLensFacing = cameraLensFacing
        cameraLensFacing = when (currentLensFacing) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
            else -> CameraSelector.LENS_FACING_FRONT
        }
        bindCamera()
    }

    override fun toggleFlash() {
        currentFlashMode = when (currentFlashMode) {
            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_OFF
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            else -> throw IllegalStateException("Unsupport flash")
        }
        imageCapture.flashMode = currentFlashMode
    }

    override fun currentFlash(): Int {
        return currentFlashMode
    }

    override fun currentTorch(): Boolean {
        return currentTorch
    }

    override fun toggleTorch() {
        currentTorch = !currentTorch
        camera.cameraControl.enableTorch(currentTorch)
    }
}