package com.test.camerax.photo

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaActionSound
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.camera.core.ImageCapture
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.test.camerax.R
import com.test.camerax.core.CameraPermissionsResolver
import com.test.camerax.core.camera.CustomCameraProvider
import com.test.camerax.core.camera.DefaultCameraProvider
import com.test.camerax.core.observeOneTimeEvent
import com.test.camerax.core.showSnackbar
import com.test.camerax.photo.state.PhotoCameraState
import kotlinx.android.synthetic.main.fragment_photo_camera.*


class PhotoCameraFragment : Fragment(R.layout.fragment_photo_camera) {

    private val viewModel: PhotoCameraViewModel by viewModels(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return PhotoCameraViewModel(requireActivity().contentResolver) as T
                }
            }
        }
    )

    private val cameraPermissionsResolver = CameraPermissionsResolver(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startCameraPreview()

        photo_camera_take_picture_button.setOnClickListener { viewModel.onTakePhotoClicked() }
        photo_camera_switch_lens_facing_button.setOnClickListener { viewModel.onSwitchLensFacingClicked() }
        photo_camera_toggle_flash_button.setOnClickListener { viewModel.onToggleFlashClicked() }
        photo_camera_toggle_torch_button.setOnClickListener { viewModel.onToggleTorchClicked() }

        viewModel.screenState.observe(
            viewLifecycleOwner,
            Observer { state -> updateScreenState(state) })

        viewModel.showMessage.observeOneTimeEvent(viewLifecycleOwner) { showSnackbar(it) }

        viewModel.showShutter.observeOneTimeEvent(viewLifecycleOwner) { showShutter() }

        viewModel.openGalley.observeOneTimeEvent(viewLifecycleOwner, ::openGallery)

    }

    private fun showShutter() {
        MediaActionSound().play(MediaActionSound.SHUTTER_CLICK)
        photo_camera_shutter_view.alpha = 0f;

        photo_camera_shutter_view
            .animate()
            .alpha(1f)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(200)
            .setListener(object : Animator.AnimatorListener {

                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    photo_camera_shutter_view.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    photo_camera_shutter_view.visibility = View.GONE
                }

                override fun onAnimationStart(animation: Animator?) {
                    photo_camera_shutter_view.visibility = View.VISIBLE
                }
            })
            .start()
    }

    @SuppressLint("MissingPermission")
    private fun startCameraPreview() {
        cameraPermissionsResolver.checkAndRequestPermissionsIfNeeded(
            onSuccess = {
                viewModel.cameraProvider =
                    DefaultCameraProvider(photo_camera_view_container, viewLifecycleOwner)
                        //CustomCameraProvider(photo_camera_view_container, viewLifecycleOwner)
                showOverlay(false)
            },
            onFail = { message ->
                showOverlay(true)
                viewModel.cameraProvider = null
                showSnackbar(message)
            }
        )
    }

    override fun onDestroyView() {
        viewModel.cameraProvider = null
        super.onDestroyView()
    }

    private fun showOverlay(show: Boolean) {
        photo_camera_overlay.visibility = if (!show) View.GONE else View.VISIBLE
    }

    private fun openGallery(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
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

        updateFlashIcon(state.flashState)
        updateTorchIcon(state.torchState)
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

    private fun animateShutter() {
        photo_camera_shutter_view
    }


}