package com.rogue.scorequest.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.rogue.scorequest.utils.ImageStorage

/**
 * Returns a click action for a "Câmera" button that captures a photo and
 * persists it via [ImageStorage]. Requests the runtime CAMERA permission
 * first when needed — launching the capture intent without it throws a
 * SecurityException on devices where the app declares the permission in
 * the manifest (required here for `android.hardware.camera` features).
 */
@Composable
fun rememberCameraCaptureAction(onCaptured: (String) -> Unit): () -> Unit {
    val context = LocalContext.current
    var pendingCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCaptureUri?.let { uri -> onCaptured(ImageStorage.persistImage(context, uri)) }
        }
    }

    fun launchCapture() {
        val uri = ImageStorage.createCaptureUri(context)
        pendingCaptureUri = uri
        takePictureLauncher.launch(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCapture()
    }

    return {
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            launchCapture()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
