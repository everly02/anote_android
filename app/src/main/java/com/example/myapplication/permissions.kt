package com.example.myapplication

//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat


enum class PermissionType {
    CAMERA, MICROPHONE, STORAGE
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CheckPermissions(permissionType: PermissionType, onPermissionGranted: () -> Unit, onPermissionDenied: () -> Unit) {
    val context = LocalContext.current
    val permissions = when (permissionType) {
        PermissionType.CAMERA -> arrayOf(Manifest.permission.CAMERA)
        PermissionType.MICROPHONE -> arrayOf(Manifest.permission.RECORD_AUDIO)
        PermissionType.STORAGE -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)

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
        PermissionType.STORAGE -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
    }
    val context = LocalContext.current
    Column(modifier =  Modifier.padding(PaddingValues(16.dp)).fillMaxWidth() ) {
        when (permissionType){
            PermissionType.CAMERA ->{
                Text("我们需要您的照相机权限")
            }

            PermissionType.MICROPHONE -> {
                Text("需要您的麦克风权限")
            }
            PermissionType.STORAGE -> {
                Text(text = "我们需要您的存储权限")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = { permissionLauncher.launch(permissions)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            }) {
            Text("去批准")
        }
    }
}
