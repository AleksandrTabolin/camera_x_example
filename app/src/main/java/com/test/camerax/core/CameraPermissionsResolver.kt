package com.test.camerax.core

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class CameraPermissionsResolver(
    private val fragment: Fragment
) {
    private var onSuccessHandler: (() -> Unit)? = null
    private var onFailHandler: ((message: String) -> Unit)? = null

    private val requestCameraPermissionLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val isCameraPermissionGranted = result[Manifest.permission.CAMERA] ?: false
            val isExternalStoragePermissionGranted = result[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if (isCameraPermissionGranted && isExternalStoragePermissionGranted) {
                onSuccessHandler?.invoke()
            } else if (isCameraPermissionGranted) {
                onFailHandler?.invoke("Camera permission required")
            } else {
                onFailHandler?.invoke("External storage permission required")
            }
        }

    fun checkAndRequestPermissionsIfNeeded(
        onSuccess: () -> Unit,
        onFail: (message: String) -> Unit
    ) {
        onSuccessHandler = onSuccess
        onFailHandler = onFail

        if (isAllPermissionsGranted()) {
            onSuccess.invoke()
        } else {
            requestCameraPermissionLauncher.launch(arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private fun isAllPermissionsGranted(): Boolean {
        return isPermissionGranted(Manifest.permission.CAMERA)
                && isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun isPermissionGranted(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(fragment.requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }
}