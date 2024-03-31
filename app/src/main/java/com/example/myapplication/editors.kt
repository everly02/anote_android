
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.example.myapplication.CheckPermissions
import com.example.myapplication.DatabaseSingleton
import com.example.myapplication.NotesScreen
import com.example.myapplication.PermissionType
import com.example.myapplication.RequestPermissionsScreen
import com.example.myapplication.db.Note
import com.example.myapplication.db.NoteType
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Composable
fun addnotescreen(navController: NavHostController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var nt = Note(type= NoteType.TEXT,
        content = "",
        title="",
        isArchived=false)
    Column(
        modifier = Modifier
            .fillMaxWidth()

            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("添加一个标题") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("输入内容") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            nt.content = content
            nt.title = title
            noteDao.insert(nt)
        }) {
            Text("确认")
        }
    }



}

@Composable
fun addrecordnote(navController: NavHostController) {
    var isRecording by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val mediaRecorder by remember { mutableStateOf(MediaRecorder()) }
    val recordLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted
            } else {
                // Handle permission denial
            }
        }
    )
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var gohome = remember{ mutableStateOf(false)}

    LaunchedEffect(key1 = true) {
        recordLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
        Button(onClick = {
            if (isRecording) {
                mediaRecorder.stop()
                mediaRecorder.release()
                isRecording = false
            } else {
                val outputFile = File(context.filesDir, "testRecording.3gp")
                mediaRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                    setOutputFile(outputFile.path)
                    prepare()
                    start()
                    isRecording = true
                    uri = Uri.fromFile(outputFile)
                }
            }
        }) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }

        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("添加一个标题") }
        )

        Button(onClick = {
            val nt = Note(
                type = NoteType.AUDIO,
                title = text,
                content = uri.toString(),
                isArchived = false
            )
            noteDao.insert(nt)
            gohome.value = true
        }) {
            Text("确认")
        }
        if (gohome.value){
            NotesScreen()
        }

    }


}


fun saveToInternalStorage(context: Context, inputStream: InputStream, fileName: String): String {
    val file = File(context.filesDir, fileName)
    try {
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
    } catch (e: IOException) {
        e.printStackTrace()
        return ""
    }
    return file.absolutePath
}
fun saveVideoToInternalStorage(context: Context, uri: Uri, fileName: String): String {
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        val filePath = saveToInternalStorage(context, inputStream, fileName)
        // 保存视频第一帧
        saveFirstFrameToInternalStorage(context, uri, "frame_${System.currentTimeMillis()}.jpg")
        return filePath
    }
    return ""
}
fun saveFirstFrameToInternalStorage(context: Context, videoUri: Uri, frameFileName: String) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, videoUri)
        val bitmap = retriever.getFrameAtTime(0)
        val frameFile = File(context.filesDir, frameFileName)
        FileOutputStream(frameFile).use { out ->
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release()
    }
}

@Composable
fun VideoPlayer(context: Context, videoUri: String) {
    val player = remember { ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(videoUri))
        prepare()
        playWhenReady = false // 默认暂停状态
    } }

    DisposableEffect(key1 = player) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
            }
        }
    )
}
@Composable
fun addvideotitle(para:Int,navController: NavHostController) {
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var nt = noteDao.getNoteById(para)

    var title by remember { mutableStateOf("") }

    Column {
        // Video Player
        VideoPlayer(context = context, videoUri = nt.content)

        // TextField for Title
        TextField(
            value = title,
            onValueChange = {
                title = it
                nt.title = title
            },
            label = { Text("添加一个标题") },
            modifier = Modifier.fillMaxWidth()
        )

        FilledTonalButton(onClick = {
            navController.navigate("notes")
        }) {
            Text("确认")
        }
    }

}
    @Composable
    fun AddVideoNote(navController: NavHostController, optype: Int) {
        val context = LocalContext.current
        var hasPermission = remember { mutableStateOf(false) }
        val db = DatabaseSingleton.getDatabase(context)
        val noteDao = db.noteDao()

        val requiredPermissionType = when (optype) {
            0 -> PermissionType.CAMERA // 对于视频录制需要摄像头权限
            1 -> PermissionType.STORAGE // 对于视频选择需要存储权限
            else -> null
        }
        requiredPermissionType?.let {
            CheckPermissions(
                permissionType = it,
                onPermissionGranted = {
                    hasPermission.value = true
                },
                onPermissionDenied = {
                    hasPermission.value = false
                }
            )
        }
        var new_code = 0
        if (hasPermission.value) {
            if (optype == 0) {

                val context = LocalContext.current
                // 初始化视频录制启动器
                val recordVideoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        result.data?.data?.let { uri ->
                            // 生成文件名
                            val fileName = "recorded_${System.currentTimeMillis()}.mp4"
                            val filePath = saveVideoToInternalStorage(context, uri, fileName)
                            val newNote = Note(
                                type = NoteType.VIDEO,
                                title = "default title video",
                                content = filePath,
                                isArchived = false
                            )
                            new_code = newNote.id
                            noteDao.insert(newNote)
                        }
                    }
                }
                recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
                addvideotitle(new_code,navController)
            } else if (optype == 1) {
                // 初始化视频选择启动器
                val pickVideoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val fileName = "selected_${System.currentTimeMillis()}.mp4"
                        val filePath = saveVideoToInternalStorage(context, uri, fileName)
                        val newNote = Note(
                            type = NoteType.VIDEO,
                            title = "default title video",
                            content = filePath,
                            isArchived = false
                        )
                        noteDao.insert(newNote)
                        new_code = newNote.id
                    }
                }
                pickVideoLauncher.launch("video/*")
                addvideotitle(new_code,navController)
            }
        } else {
            requiredPermissionType?.let {
                RequestPermissionsScreen(permissionType = it) {

                }
            } ?: run {
            }
        }
    }


