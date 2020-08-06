package com.test.camerax.core

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar

fun <T> MutableLiveData<OneTimeEvent<T>>.postOneTimeEvent(event: T) {
    postValue(OneTimeEvent(event));
}

fun <T> MutableLiveData<OneTimeEvent<T>>.observeOneTimeEvent(
    owner: LifecycleOwner,
    block: (T) -> Unit
) {
    observe(owner, Observer { state -> state.process(block) })
}

fun Fragment.showSnackbar(text: String) {
    Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
}

fun Context.getProcessCameraProvider(onDone: (ProcessCameraProvider) -> Unit) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
    cameraProviderFuture.addListener(
        Runnable { onDone.invoke(cameraProviderFuture.get()) },
        ContextCompat.getMainExecutor(this)
    )
}