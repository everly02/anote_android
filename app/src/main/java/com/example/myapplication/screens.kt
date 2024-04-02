package com.example.myapplication


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
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
import androidx.core.content.ContextCompat
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
fun PermissionExplanationDialog(showDialog: Boolean, onUpdateShowDialog: (Boolean) -> Unit) {
    val context = LocalContext.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // 用户点击对话框外部时触发，更新对话框显示状态
                onUpdateShowDialog(false)
            },
            title = {
                Text(text = "权限请求")
            },
            text = {
                Text("我们需要照相机权限来继续。请在设置中授权。")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 导航到应用设置页面
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                        onUpdateShowDialog(false) // 关闭对话框
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                Button(onClick = { onUpdateShowDialog(false) }) {
                    Text("取消")
                }
            }
        )
    }
}


fun hasCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}


@SuppressLint("QueryPermissionsNeeded")
@Composable
fun NotesScreen(nav:NavController) {
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
    var addtitle = remember {
        mutableStateOf(false)
    }

    var showpermissiondia= remember { mutableStateOf(false)}
    if (showDialog) {


        var vaddress=""
        val videoCaptureLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("OKOKOKOKOKOKOKOK","YEEEEEEEEEEEEEEEEE")
                val videoUri: Uri? = result.data?.data
                vaddress = videoUri.toString()
                addtitle.value=true
            }
            else{
                Log.d("OKOKOKOKOKOKOKOK","NNONONONONONONONONONO")
            }
        }

        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
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

                                            showDialog = false
                                            if (!hasCameraPermission(context)) {
                                                // 如果没有相机权限，显示对话框
                                                showpermissiondia.value = true
                                            } else {
                                                if (videoIntent.resolveActivity(context.packageManager) != null) {
                                                    videoCaptureLauncher.launch(videoIntent)
                                                    addtitle.value=true
                                                } else {
                                                    // 没有找到合适的应用来处理录制
                                                }
                                            }

                                        }

                                        "选择文件" -> {

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
        if(showpermissiondia.value == true){
            PermissionExplanationDialog(showDialog = true) {
            }
        }
        if (addtitle.value) {
            // 当addTitle为true时，渲染addvedioscreen
            addvedioscreen(addr=vaddress)
        }
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

                    val framePath = note.previewImage
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


