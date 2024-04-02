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
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.TextField
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
import java.io.IOException
import java.io.InputStream

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
fun addvideotitle(para:Int,navController: NavHostController) {
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var nt by remember { mutableStateOf<Note?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val viewModel: EditorViewModel = viewModel(factory = EditorViewModelFactory(noteDao))
    // 使用LaunchedEffect启动协程调用挂起函数
    LaunchedEffect(key1 = para) {
        nt = viewModel.getNoteById(para) // 确保这是在viewModel内部合适地处理
    }

    var title by remember { mutableStateOf("") }

    Column {
        // Video Player
        nt?.let { VideoPlayer(context = context, videoUri = it.content) }

        // TextField for Title
        TextField(
            value = title,
            onValueChange = {
                title = it
                nt ?.title = title
            },
            label = { Text("添加一个标题") },
            modifier = Modifier.fillMaxWidth()
                                .padding(8.dp)
        )

        FilledTonalButton(onClick = {
            coroutineScope.launch {
                nt?.let { viewModel.update_nt(it) }
            }
            navController.navigate("notes")
        }) {
            Text("确认")
        }
    }

}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AddVideoNote(optype: Int, navController: NavHostController) {




}
@Composable
fun addnotescreen(navi:NavController) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    var viewmodel = EditorViewModel(noteDao)

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



