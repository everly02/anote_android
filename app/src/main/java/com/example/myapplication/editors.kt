
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.myapplication.CheckPermissions
import com.example.myapplication.DatabaseSingleton
import com.example.myapplication.NotesScreen
import com.example.myapplication.PermissionType
import com.example.myapplication.RequestPermissionsScreen
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Composable
fun addnotescreen() {

}

@Composable
fun addrecordnote(){

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
fun addvideotitle(para:Int) {
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var nt = noteDao.getNoteById(para)

    var title by remember { mutableStateOf("") }
    var gohome = remember{ mutableStateOf(false)}
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
            gohome.value=true
        }) {
            Text("确认")
        }
    }
    if (gohome.value == true){
        NotesScreen()
    }
}
    @Composable
    fun AddVideoNote(optype: Int) {
        val context = LocalContext.current
        var hasPermission = remember { mutableStateOf(false) }
        val db = DatabaseSingleton.getDatabase(context)
        val noteDao = db.noteDao()
        // 根据optype确定所需的权限类型
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
                addvideotitle(new_code)
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
                addvideotitle(new_code)
            }
        } else {
            requiredPermissionType?.let {
                RequestPermissionsScreen(permissionType = it) {

                }
            } ?: run {
            }
        }
    }


