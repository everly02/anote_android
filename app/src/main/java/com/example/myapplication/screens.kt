package com.example.myapplication

import Note
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.db.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.withContext

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    val unarchivedNotes: LiveData<List<Note>> = noteDao.getUnarchivedNotes().asLiveData(viewModelScope.coroutineContext)
}
@Composable
fun NotesScreen() {
    val noteViewModel: NoteViewModel = viewModel()
    // 使用 LiveData 的扩展函数 observeAsState 来观察 LiveData 对象
    val notes by noteViewModel.unarchivedNotes.observeAsState(initial = emptyList())
    Scaffold(
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = { /* 处理record点击事件 */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record")
                    }
                    IconButton(onClick = { /* 处理video点击事件 */ }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video")
                    }
                    IconButton(onClick = { /* 处理alert点击事件 */ }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alert")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { /* do something */ },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Localized description")
                    }
                }
            )
        },
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No unarchived notes found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(notes) { note ->
                    NoteItem(note)
                }
            }
        }
    }
}
@Composable
fun VideoFrameImage(videoPath: String) {

    val bitmapState = produceState<Bitmap?>(initialValue = null, videoPath) {
        value = withContext(Dispatchers.IO) {
            getVideoFrame(videoPath)
        }
    }

    bitmapState.value?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Video Frame",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    } ?: Text("Loading video frame...")
}
suspend fun getVideoFrame(videoPath: String): Bitmap {
    val retriever = MediaMetadataRetriever()
    return withContext(Dispatchers.IO) { // 确保在IO线程执行
        retriever.setDataSource(videoPath)
        // 尝试获取视频的第一帧
        retriever.getFrameAtTime(0) ?: throw IllegalArgumentException("Unable to extract frame.")
    }.also {
        retriever.release() // 释放资源
    }
}

@Composable
fun NoteItem(note: Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ){
        Column(modifier = Modifier.padding(16.dp)) {
            when (note.type) {
                NoteType.VIDEO -> {
                    // 如果是视频类型，显示视频帧
                    val bitmap = remember { getVideoFrame(note.content) } // 假设note.content包含视频路径
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Video Frame",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    // 其他类型的处理
                    Text(text = note.content)
                }
            }
        }
    }
}
@SuppressLint("RememberReturnType")
@Composable
fun TodoScreen() {
    val todos = remember { mutableStateListOf<String>() }
    val textState = remember { mutableStateOf(TextFieldValue()) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                actions = {},
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            if (textState.value.text.isNotBlank()) {
                                todos.add(textState.value.text)
                                textState.value = TextFieldValue("") // 清空输入框
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Add, "Add")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TextField(
                value = textState.value,
                onValueChange = { textState.value = it },
                label = { Text("Add a todo") }
            )
            LazyColumn {
                items(todos) { todo ->
                    Text(todo, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}


@Composable
fun ArchivedScreen() {
    Text("已归档内容", modifier = Modifier.padding(16.dp))
}


