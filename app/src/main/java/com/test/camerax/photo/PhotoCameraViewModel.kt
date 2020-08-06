package com.test.camerax.photo

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.camerax.core.camera.CameraProvider
import com.test.camerax.core.OneTimeEvent
import com.test.camerax.core.postOneTimeEvent
import com.test.camerax.photo.state.PhotoCameraState
import com.test.camerax.usecase.photo.SavePhotoUseCase
import kotlinx.coroutines.launch

class PhotoCameraViewModel(
    private val contentResolver: ContentResolver
) : ViewModel() {

    private val savePhotoUseCase = SavePhotoUseCase()

    val screenState = MutableLiveData<PhotoCameraState>()
    val showMessage = MutableLiveData<OneTimeEvent<String>>()

    val showShutter = MutableLiveData<OneTimeEvent<Boolean>>()

    val openGalley = MutableLiveData<OneTimeEvent<Uri>>()

    var cameraProvider: CameraProvider? = null
        set(value) {
            if (value != null) {
                updateScreenState(
                    currentScreenState.copy(
                        torchState = value.currentTorch(),
                        flashState = value.currentFlash()
                    )
                )
            }
            field = value
        }

    private val currentScreenState: PhotoCameraState
        get() = screenState.value ?: PhotoCameraState.INIT

    fun onSwitchLensFacingClicked() {
        val cameraProvider = cameraProvider ?: return

        cameraProvider.switchLensFacing()
    }

    fun onToggleFlashClicked() {
        val cameraProvider = cameraProvider ?: return

        cameraProvider.toggleFlash()

        updateScreenState(
            currentScreenState.copy(
                flashState = cameraProvider.currentFlash()
            )
        )
    }

    fun onToggleTorchClicked() {
        val cameraProvider = cameraProvider ?: return
        cameraProvider.toggleTorch()

        updateScreenState(
            currentScreenState.copy(
                torchState = cameraProvider.currentTorch()
            )
        )
    }

    fun onTakePhotoClicked() {
        val cameraProvider = cameraProvider ?: return

        makeCameraAction {
            cameraProvider.takePicture()?.let { image ->
                try {
                    savePhotoUseCase.savePhoto(image, contentResolver)?.let { uri ->
                        openGalley.postOneTimeEvent(uri)
                    }
                } catch (error: Throwable) {
                    Log.e("takePhoto", "error", error)
                    showMessage.postOneTimeEvent("takePhoto: error")
                }
            }
        }
    }

    private fun makeCameraAction(block: suspend () -> Unit) {
        if (currentScreenState.enableInput) {
            viewModelScope.launch {
                showShutter.postOneTimeEvent(true)
                updateScreenState(currentScreenState.copy(enableInput = false))
                try {
                    block.invoke()
                } catch (error: Throwable) {
                    Log.e("Camera", "error", error)
                    showMessage.postOneTimeEvent("Something went wrong")
                } finally {
                    updateScreenState(currentScreenState.copy(enableInput = true))
                }
            }
        }
    }

    private fun updateScreenState(state: PhotoCameraState) {
        screenState.value = state
    }
}