package com.example.myapplication

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                noteapp(navController = navController)
            }
        }
    }
}
    enum class DrawerScreens(val displayName: String) {
        Notes("笔记"),
        Todo("待办"),
        Archived("已归档"),
        Screen("")
    }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun noteapp(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf(DrawerScreens.Notes) }
    val navController = rememberNavController()


    MaterialTheme {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("NAV", modifier = Modifier.padding(16.dp))
                    Divider()
                    DrawerScreens.values().forEach { screen ->
                        val isSelected = screen == currentScreen
                        NavigationDrawerItem(
                            label = { Text(text = screen.displayName) },
                            selected = isSelected,
                            onClick = {
                                currentScreen = screen

                                scope.launch { drawerState.close() }
                            }

                        )
                    }

                }
            }
        ) {
            Scaffold(
                topBar = {

                    TopAppBar(
                        title = { Text(text = currentScreen.displayName) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = {}) {
                                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                            }
                        }
                    )


                }
            ) {

            }
            when (currentScreen) {
                DrawerScreens.Notes -> NotesScreen()
                DrawerScreens.Todo -> TodoScreen()
                DrawerScreens.Archived -> ArchivedScreen()
                DrawerScreens.Screen -> otherscreen()
            }
        }

    }

}



@Composable
fun otherscreen() {
    //各自渲染
}




