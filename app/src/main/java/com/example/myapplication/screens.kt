package com.example.myapplication


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.room.Room
import com.example.myapplication.db.AppDatabase
import com.example.myapplication.db.Note
import com.example.myapplication.db.NoteDao
import com.example.myapplication.db.NoteType
import kotlinx.coroutines.launch

class NoteViewModel(noteDao: NoteDao) : ViewModel() {
    val unarchivedNotes: LiveData<List<Note>> = noteDao.getUnarchivedNotes().asLiveData(viewModelScope.coroutineContext)

}

object DatabaseSingleton {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "main_database.db")
                    .fallbackToDestructiveMigration() // 在版本更新丢失数据时使用
                    .build()
            }
        }
        return INSTANCE!!
    }
}

@Composable
fun NotesScreen(nav:NavController) {
    Log.d("notescreen running","yes")
    // 使用 LiveData 的扩展函数 observeAsState 来观察 LiveData 对象

    var showDialog by remember { mutableStateOf(false) }
    val items = listOf("拍摄", "选择文件") // 选项列表

    val context = LocalContext.current

    val noteViewModel: NoteViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @SuppressLint("SuspiciousIndentation")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    val db = DatabaseSingleton.getDatabase(context)
                    val noteDao = db.noteDao()
                        return NoteViewModel(noteDao) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    val coroutineScope = rememberCoroutineScope()
    val viewModel: EditorViewModel = viewModel(factory = EditorViewModelFactory(noteDao))
    var new_code = 0

    val notes by noteViewModel.unarchivedNotes.observeAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                actions = {
                    IconButton(onClick = { nav.navigate("addr") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record")
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video")
                    }
                    IconButton(onClick = {  }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alert")
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { nav.navigate("addn") },
                        containerColor = MaterialTheme.colorScheme.primary,
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

                    NoteItem(note){
                        val i=note.id
                        nav.navigate("note_detail/$i")
                    }
                }
            }
        }

    }
    if (showDialog) {
        Log.d("dialog showed","showed")
        val recordVideoLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) {
            result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("IF VEDIO TAKEN SUCCEEDED","YE")
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
                    Log.d("newNote over","over")
                    new_code = newNote.id
                    Log.d("about to start coroutinescoupe","about")
                    coroutineScope.launch {
                        try {

                            // 假设 insert_nt 是正确定义的，并且可以正确执行
                            viewModel.insert_nt(newNote)
                            // 操作成功后的逻辑，如导航
                            nav.navigate("addvt/$new_code")
                        } catch (e: Exception) {
                            Log.e("RecordVideo", "Failed to insert note or navigate", e)
                            // 处理异常，如显示错误消息

                        }
                    }
                }
            }
            else{
                Log.d("IF VEDIO TAKEN SUCCEEDED","NO")
            }
        }

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
                coroutineScope.launch {
                    viewModel.insert_nt(newNote)
                    Log.d("if database vedio inserting done","it is done")
                }
                new_code = newNote.id
            }
        }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "请选择") },
            text = {
                Column {
                    items.forEach { item ->
                        Text(
                            text = item,
                            modifier = Modifier
                                .clickable {
                                    when (item) {
                                        "拍摄" -> {
                                            recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
                                            nav.navigate("addvt/$new_code")
                                        }

                                        "选择文件" -> {
                                            pickVideoLauncher.launch("video/*")
                                            nav.navigate("addvt/$new_code")
                                        }
                                    }
                                    showDialog = false
                                }
                                .padding(16.dp), // 增加内边距来调整尺寸
                            fontSize = 20.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("取消")
                }
            },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        )
    }


}
@Composable
fun NoteItem(note: Note,onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant)

    ){
        Column(modifier = Modifier.padding(16.dp)) {
            when (note.type) {
                NoteType.VIDEO -> {
                    val context = LocalContext.current
                    val frameFileName = note.content.replace("recorded_", "frame_")
                        .replace("selected_", "frame_")
                        .replace(".mp4", ".jpg")
                    val framePath = context.filesDir.absolutePath + "/" + frameFileName
                    val bitmap = BitmapFactory.decodeFile(framePath)
                    bitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Video Frame",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    } ?: Text("视频预览无法加载")

                }
                else -> {
                    // 其他类型的处理
                    Text(text = note.title)
                }
            }
        }
    }
}
@SuppressLint("RememberReturnType")
@Composable
fun TodoScreen(nav:NavController) {
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
fun ArchivedScreen(nav:NavController) {
    Text("已归档内容", modifier = Modifier.padding(16.dp))
}


