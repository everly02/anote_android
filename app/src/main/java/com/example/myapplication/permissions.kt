package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


enum class PermissionType {
    CAMERA, MICROPHONE, STORAGE
}
@Composable
fun CheckPermissions(permissionType: PermissionType, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val context = LocalContext.current
    val permissions = when (permissionType) {
        PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
        PermissionType.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
        PermissionType.STORAGE -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    }

    val allPermissionsGranted = permissions.all { permission: String ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    if (allPermissionsGranted) {
        onPermissionGranted()
    } else {
        onPermissionDenied()
    }
}
@Composable
fun RequestPermissionsScreen(permissionType: PermissionType, onPermissionsGranted: () -> Unit) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            onPermissionsGranted()
        } else {
            // Handle permission denial
        }
    }

    val permissions = when (permissionType) {
        PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
        PermissionType.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
        PermissionType.STORAGE -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    Column {
        when (permissionType){
            PermissionType.CAMERA ->{
                Text("我们需要您的照相机权限")
            }

            PermissionType.MICROPHONE -> {
                Text("需要您的麦克风权限")
            }
            PermissionType.STORAGE -> {
                Text(text = "需要存储权限")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { permissionLauncher.launch(permissions) }) {
            Text("去批准")
        }
    }
}
