package com.eli.anote.screens

import DatabaseSingleton
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eli.anote.db.Note

@Composable
fun ArchivedScreen(nav: NavController) {
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
    val notes by noteViewModel.archivedNotes.observeAsState(initial = emptyList())

    var nt = remember {
        mutableStateOf<Note?>(null)
    }

    Scaffold() { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No archived notes found")
            }
        } else {
            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(notes) { note ->
                    NoteItem(note,
                        onClick = {
                            val i = note.id
                            nav.navigate("note_detail/$i")
                        },
                        onLongClick = {})
                }
            }
        }
    }
}