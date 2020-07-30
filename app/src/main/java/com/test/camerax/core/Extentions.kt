package com.test.camerax.core

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun CameraView.takePicture(): ImageProxy {
    return suspendCoroutine { continuation ->
        takePicture(
            ContextCompat.getMainExecutor(context),
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

fun <T> MutableLiveData<OneTimeEvent<T>>.postOneTimeEvent(event: T) {
    postValue(OneTimeEvent(event));
}


fun <T> MutableLiveData<OneTimeEvent<T>>.observeOneTimeEvent(owner: LifecycleOwner, block: (T) -> Unit) {
    observe(owner, Observer { state ->state.process(block) })
}
