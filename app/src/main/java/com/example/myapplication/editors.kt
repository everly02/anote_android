package com.example.myapplication
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries
//noinspection UsingMaterialAndMaterial3Libraries

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.myapplication.db.Note
import com.example.myapplication.db.NoteDao
import com.example.myapplication.db.NoteType
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditorViewModelFactory(private val noteDao: NoteDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(noteDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
class EditorViewModel(private val dao:NoteDao): ViewModel() {
    fun insert_nt(nt:Note) {
        viewModelScope.launch {
            dao.insert(nt)
        }
    }
    suspend fun getNoteById(id: Int): Note {
        return dao.getNoteById(id)
    }

    fun update_nt(nt:Note){
        viewModelScope.launch{
            dao.update(nt)
        }
    }
}
@Composable
fun VideoPreview(addr: Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(addr)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // 显示控制器
            }
        },
        modifier = Modifier.fillMaxWidth(),
        update = { view ->
            view.player = exoPlayer
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}
fun captureFrameAndSave(context: Context, videoUriString: String): Uri? {
    val retriever = MediaMetadataRetriever()
    try {
        // 使用MediaMetadataRetriever从视频Uri获取帧
        retriever.setDataSource(context, Uri.parse(videoUriString))
        val frame: Bitmap? = retriever.getFrameAtTime()

        frame?.let {
            // 创建图片文件
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "JPEG_${timestamp}_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val imageFile = File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )

            // 将帧保存为图片
            FileOutputStream(imageFile).use { out ->
                frame.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // 返回保存的图片文件的Uri
            return Uri.fromFile(imageFile)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        retriever.release()
    }
    return null
}
@Composable

fun addvedioscreen(addr: String, navi: NavHostController){
    var title by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    val viewmodel: EditorViewModel = viewModel(
        factory = EditorViewModelFactory(noteDao)
    )
    val pre_img= (captureFrameAndSave(context,addr))?.path
    var nt = Note(type= NoteType.VIDEO,
        content = addr,
        title="",
        previewImage = pre_img,
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
        VideoPreview(addr = Uri.parse(addr))
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            nt.title = title
            viewmodel.insert_nt(nt)
            navi.navigate("notes")
        }) {
            Text("确认")
        }
    }
}
@Composable
fun addnotescreen(navi:NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()

    val viewmodel: EditorViewModel = viewModel(
        factory = EditorViewModelFactory(noteDao)
    )

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
                               .padding(vertical = 24.dp)

        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            nt.content = content
            nt.title = title
            viewmodel.insert_nt(nt)
            navi.navigate("notes")
        }) {
            Text("确认")
        }
    }
}

@Composable
fun addrecordnote(navi: NavHostController) {
    var isRecording by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val mediaRecorder by remember { mutableStateOf(MediaRecorder()) }
    val coroutineScope = rememberCoroutineScope()
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    val viewModel: EditorViewModel = viewModel(factory = EditorViewModelFactory(noteDao))
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

    remember{ mutableStateOf(false)}

    LaunchedEffect(key1 = true) {
        recordLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(modifier = Modifier.padding(PaddingValues(16.dp)).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        FilledTonalButton(
            modifier = Modifier.size(width = 250.dp, height = 70.dp),
            onClick = {
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
        Spacer(modifier = Modifier.height(30.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("添加一个标题") }
        )
        Spacer(modifier = Modifier.height(30.dp))
        Button(onClick = {
            val nt = Note(
                type = NoteType.AUDIO,
                title = text,
                content = uri.toString(),
                isArchived = false
            )
            if (context is Activity) {
                coroutineScope.launch {
                    viewModel.insert_nt(nt)
                }
            }
            navi.navigate("notes")
        })
        {
            Text("确认")
        }

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



