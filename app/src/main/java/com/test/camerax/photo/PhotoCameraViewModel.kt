package com.test.camerax.photo

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.camerax.core.CameraProvider
import com.test.camerax.core.OneTimeEvent
import com.test.camerax.core.postOneTimeEvent
import com.test.camerax.photo.state.PhotoCameraState
import com.test.camerax.usecase.photo.SavePhotoUseCase
import kotlinx.coroutines.launch

class PhotoCameraViewModel : ViewModel() {

    private val savePhotoUseCase = SavePhotoUseCase()

    val screenState = MutableLiveData<PhotoCameraState>()
    val showMessage = MutableLiveData<OneTimeEvent<String>>()

    val openGalley = MutableLiveData<OneTimeEvent<Uri>>()

    var cameraProvider: CameraProvider? = null

    private val currentScreenState: PhotoCameraState
        get() = screenState.value ?: PhotoCameraState.INIT

    fun onTakePhotoClicked() {
        val cameraProvider = cameraProvider ?: return
        makeCameraAction {
            cameraProvider.takePicture()?.let { image ->
                try {
                    savePhotoUseCase.savePhoto(image, cameraProvider.provideContentResolver())?.let { uri ->
                        openGalley.postOneTimeEvent(uri)
                    }
                } catch(error: Throwable) {
                    Log.e("takePhoto", "error", error)
                    showMessage.postOneTimeEvent("takePhoto: error")
                }
            }
        }
    }

    private fun makeCameraAction(block: suspend () -> Unit) {
        if (currentScreenState.enableInput) {
            viewModelScope.launch {
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