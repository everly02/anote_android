package com.example.myapplication
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController
import com.example.myapplication.db.Note
import com.example.myapplication.db.NoteType

@Composable
fun noteviewer(nav:NavHostController,id:Int){
    val context = LocalContext.current
    val db = DatabaseSingleton.getDatabase(context)
    val noteDao = db.noteDao()
    val coroutineScope = rememberCoroutineScope()
    val viewModel: EditorViewModel = viewModel(factory = EditorViewModelFactory(noteDao))
    // 使用LaunchedEffect启动协程调用挂起函数
    var note by remember { mutableStateOf<Note?>(null) }
    LaunchedEffect(key1 = id) {
        note = viewModel.getNoteById(id) // 确保这是在viewModel内部合适地处理
    }

    Column(modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        if (note != null) {
            Text(text = "Title: ${note!!.title}", style = MaterialTheme.typography.titleLarge)
        }
        else{
            Text("note null detected")
        }

        if (note != null) {
            when (note!!.type) {
                NoteType.TEXT -> Text(text = "Content: ${note!!.content}", style = MaterialTheme.typography.bodyLarge)
                NoteType.VIDEO, NoteType.AUDIO -> MediaContent(note!!, LocalContext.current)
            }
        }
    }
}
@Composable
fun MediaContent(note: Note, context: Context) {

    val media3Player = remember { ExoPlayer.Builder(context).build() }

    // Prepare and play the media item
    DisposableEffect(context) {
        val mediaItem = MediaItem.fromUri(Uri.parse(note.content))
        media3Player.setMediaItem(mediaItem)
        media3Player.prepare()
        media3Player.playWhenReady = true

        onDispose {
            media3Player.release()
        }
    }

    // Use AndroidView to integrate the Media3 PlayerView
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = media3Player
                useController = true // Enable or disable controller based on need
            }
        },
        modifier = Modifier.fillMaxWidth().height(450.dp)
    )
}

// Helper function to remember and initialize Media3 ExoPlayer instance
