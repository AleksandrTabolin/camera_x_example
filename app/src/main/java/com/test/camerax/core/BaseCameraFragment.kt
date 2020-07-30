package com.test.camerax.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_photo_camera.*

abstract class BaseCameraFragment(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId)  {

    private var cameraView: CameraView? = null

    var isCameraBindedToLifecycle: Boolean = false

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            handleCameraLifecycleBinding(cameraView)
        }

    private val requestExternalStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!isExternalStoragePermissionGranted()) {
                showSnackbar("External storage permission required")
            }
        }

    fun startCameraPreview(cameraView: CameraView) {
        onCameraBindedToLifecycle(false)
        this.cameraView = cameraView

        if (!handleCameraLifecycleBinding(cameraView)) {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    protected suspend fun takePictureInternal(): ImageProxy? {
        return if (isExternalStoragePermissionGranted()) {
            cameraView?.takePicture()
        } else {
            requestExternalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            showSnackbar("External storage permission required")
            null
        }
    }

    override fun onDestroyView() {
        cameraView = null
        super.onDestroyView()
    }

    protected fun showSnackbar(text: String) {
        Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun handleCameraLifecycleBinding(cameraView: CameraView?): Boolean {
        val view = cameraView ?: return false

        return if (isCameraPermissionGranted()) {
            view.bindToLifecycle(viewLifecycleOwner)
            isCameraBindedToLifecycle = true
            onCameraBindedToLifecycle(true)
            true
        } else {
            showSnackbar("Camera permission required")
            false
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isExternalStoragePermissionGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    open fun onCameraBindedToLifecycle(isBinded: Boolean) {

    }

}