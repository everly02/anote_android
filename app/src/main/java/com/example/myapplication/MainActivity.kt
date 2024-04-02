package com.example.myapplication

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.db.Note
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    fun setNote(note: Note) {
        _note.value = note
    }
}
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var string_to_show = remember{ mutableStateOf("笔记") }
    val sharedViewModel: SharedViewModel = viewModel()
    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                Divider()
                DrawerItem("Notes", selected = false, onClick = { navigateToScreen("notes", drawerState, scope, navController)
                    string_to_show.value="笔记"})
                DrawerItem("Todo", selected = false, onClick = { navigateToScreen("todo", drawerState, scope, navController)
                    string_to_show.value="待办清单"})
                DrawerItem("Archived", selected = false, onClick = { navigateToScreen("archived", drawerState, scope, navController)
                    string_to_show.value="已归档"})
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(string_to_show.value) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(navController = navController, startDestination = "notes", Modifier.padding(paddingValues)) {
                composable("notes") { NotesScreen(navController) }
                composable("todo") { TodoScreen(navController) }
                composable("archived") { ArchivedScreen(navController) }

                composable("addr"){ addrecordnote(navController) }
                composable("addn"){ addnotescreen(navController) }
                composable("addv/{uri}",arguments = listOf(navArgument("uri") { type = NavType.StringType })){
                    backStackEntry ->
                    val videoAddr = backStackEntry.arguments?.getString("uri") ?: ""
                    addvedioscreen(videoAddr, navController)
                }
                composable("note_detail/{id}"){backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toInt() ?: -1
                    noteviewer(navController,id)
                }

            }
        }
    }
}

@Composable
fun DrawerItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(text = label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
fun navigateToScreen(route: String, drawerState: DrawerState, scope: CoroutineScope, navController: NavController) {
    scope.launch {
        drawerState.close() // Close the drawer
        navController.navigate(route) // Navigate
    }
}
