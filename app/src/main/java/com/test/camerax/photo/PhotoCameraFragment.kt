package com.test.camerax.photo

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.test.camerax.R
import com.test.camerax.core.BaseCameraFragment
import com.test.camerax.core.CameraProvider
import com.test.camerax.core.observeOneTimeEvent
import com.test.camerax.photo.state.PhotoCameraState
import kotlinx.android.synthetic.main.fragment_photo_camera.*


class PhotoCameraFragment : BaseCameraFragment(R.layout.fragment_photo_camera), CameraProvider {

    private val viewModel: PhotoCameraViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startCameraPreview(photo_camera_view)
        viewModel.cameraProvider = this

        photo_camera_take_picture_button.setOnClickListener { viewModel.onTakePhotoClicked() }
        photo_camera_switch_lens_facing_button.setOnClickListener { switchLensFacing() }
        photo_camera_toggle_flash_button.setOnClickListener { toggleFlash() }
        photo_camera_toggle_torch_button.setOnClickListener { toggleTorch() }

        viewModel.screenState.observe(
            viewLifecycleOwner,
            Observer { state -> updateScreenState(state) })

        viewModel.showMessage.observeOneTimeEvent(viewLifecycleOwner, ::showSnackbar)

        viewModel.openGalley.observeOneTimeEvent(viewLifecycleOwner, ::openGallery)

    }

    override fun onDestroyView() {
        viewModel.cameraProvider = null
        super.onDestroyView()
    }

    override fun onCameraBindedToLifecycle(isBinded: Boolean) {
        photo_camera_overlay.visibility = if (isBinded) View.GONE else View.VISIBLE

        if (isBinded) {
            updateFlashIcon(photo_camera_view.flash)
            updateTorchIcon(photo_camera_view.isTorchOn)
        }
    }

    override suspend fun takePicture(): ImageProxy? {
        if (!isCameraBindedToLifecycle) return null
        val result = takePictureInternal()
        updateFlashIcon(photo_camera_view.flash)
        updateTorchIcon(photo_camera_view.isTorchOn)

        ContextCompat.getExternalFilesDirs(requireContext(), "");
        return result;
    }

    private fun openGallery(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
    override fun provideContentResolver(): ContentResolver {
        return requireActivity().contentResolver;
    }

    override fun switchLensFacing() {
        val currentLensFacing = photo_camera_view.cameraLensFacing
        photo_camera_view.cameraLensFacing = when (currentLensFacing) {
            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
            else -> CameraSelector.LENS_FACING_FRONT
        }
    }

     private fun toggleFlash() {
         val nextFlash = when (photo_camera_view.flash) {
             ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_OFF
             ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
             ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
             else -> throw IllegalStateException("Unsupport flash")
         }

         updateFlashIcon(nextFlash)
         photo_camera_view.flash = nextFlash
     }

    private fun toggleTorch() {
        val nextTorch = !photo_camera_view.isTorchOn
        photo_camera_view.enableTorch(nextTorch)
        updateTorchIcon(nextTorch)
    }

    private fun updateTorchIcon(isTorchOn: Boolean) {
        val icon = if (isTorchOn) {
            R.drawable.ic_baseline_torch_on_24
        } else {
            R.drawable.ic_baseline_torch_off_24
        }
        photo_camera_toggle_torch_button.setImageResource(icon)
    }

    private fun updateFlashIcon(flash: Int) {
        val icon = when (flash) {
            ImageCapture.FLASH_MODE_AUTO -> R.drawable.ic_baseline_flash_auto_24
            ImageCapture.FLASH_MODE_ON -> R.drawable.ic_baseline_flash_on_24
            ImageCapture.FLASH_MODE_OFF -> R.drawable.ic_baseline_flash_off_24
            else -> throw IllegalStateException("Unsupport flash")
        }
        photo_camera_toggle_flash_button.setImageResource(icon)
    }

    private fun updateScreenState(state: PhotoCameraState) {
        listOf(
            photo_camera_switch_lens_facing_button,
            photo_camera_toggle_flash_button,
            photo_camera_toggle_torch_button,
            photo_camera_take_picture_button
        ).forEach { view ->
            view?.isEnabled = state.enableInput
            view?.alpha = if (state.enableInput) 1f else 0.5f
        }
    }

}