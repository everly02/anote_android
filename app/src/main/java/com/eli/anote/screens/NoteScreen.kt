package com.eli.anote.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.eli.anote.db.Note
import com.eli.anote.db.NoteDao
import com.eli.anote.db.NoteType
import kotlinx.coroutines.launch

class NoteViewModel(private val noteDao: NoteDao) : ViewModel() {
    val unarchivedNotes: LiveData<List<Note>> = noteDao.getUnarchivedNotes().asLiveData(viewModelScope.coroutineContext)
    val archivedNotes: LiveData<List<Note>> = noteDao.getarchievedNotes().asLiveData(viewModelScope.coroutineContext)
    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteDao.update(note)
        }
    }
    suspend fun getnote(id:Int):Note {

        return noteDao.getNoteById(id)

    }
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.delete(note)
        }
    }
}


@SuppressLint("QueryPermissionsNeeded")
@Composable
fun NotesScreen(nav: NavController) {
    val context = LocalContext.current
    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri = result.data?.data // 获取视频URI
            videoUri?.let {
                // 导航并传递URI
                val uriString = Uri.encode(videoUri.toString())
                nav.navigate("addv/$uriString")
            }
        }
    }
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
    val notes by noteViewModel.unarchivedNotes.observeAsState(initial = emptyList())
    var showdialog = remember{ mutableStateOf(false) }
    var dia_title = remember {
        mutableStateOf("")
    }
    var nt = remember {
        mutableStateOf<Note?>(null)
    }

    val hasPermissions = remember { mutableStateOf(false) }

    // Launcher for camera permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions.value = permissions.values.all { it }
        if (hasPermissions.value) {
            // Permissions granted, launch camera
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            if (intent.resolveActivity(context.packageManager) != null) {
                startForResult.launch(intent)
            }
        }
    }

    if (showdialog.value) {
        remember {
            mutableStateOf<Note?>(null)
        }
        Dialog(onDismissRequest = { showdialog.value = false }) {

            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "选择操作", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        nt.value?.let { currentNote ->
                            val updatedNote = currentNote.copy(isArchived = true)

                            nt.value = updatedNote

                            noteViewModel.updateNote(updatedNote)
                        }
                        showdialog.value = false // 关闭对话框
                    }) {
                        Text("归档",style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        nt.value?.let{

                            noteViewModel.deleteNote(nt.value!!)
                        }
                        showdialog.value = false // 关闭对话框
                    }) {
                        Text("删除", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                TextButton(
                    onClick = { showdialog.value = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),

                    ) {
                    Text("取消")
                }
            }
        }
    }
    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                actions = {
                    IconButton(onClick = { nav.navigate("addr") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Record")
                    }
                    IconButton(onClick = {
                        if (!hasPermissions.value) {
                            permissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.CAMERA,
                                    android.Manifest.permission.READ_MEDIA_VIDEO
                                )
                            )
                        } else {

                            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                startForResult.launch(intent)
                            }
                        }
                    }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video")
                    }
                    IconButton(onClick = { }) {
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
                    dia_title.value = note.title
                    NoteItem(note,
                        onClick = {
                            val i = note.id
                            nav.navigate("note_detail/$i")
                        },
                        onLongClick = {
                            showdialog.value = true
                            nt.value=note
                        })
                }
            }
        }
    }

}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(note: Note, onClick: () -> Unit, onLongClick: ()->Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant)

    ){
        Column(modifier = Modifier.padding(16.dp)) {
            when (note.type) {
                NoteType.VIDEO -> {

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