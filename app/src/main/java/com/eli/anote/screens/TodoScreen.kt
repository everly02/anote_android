package com.eli.anote.screens

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.eli.anote.db.DatabaseBuilder
import com.eli.anote.db.TodoDao
import com.eli.anote.db.TodoItem
import kotlinx.coroutines.launch


class TodoViewModelFactory(private val repository: TodoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TodoRepository(private val dao: TodoDao) {
    val todos : LiveData<List<TodoItem>> = dao.getAllTodos()

    suspend fun addTodo(todo: TodoItem) {
        dao.insert(todo)
    }

    suspend fun updateTodo(todo: TodoItem) {
        dao.update(todo)
    }

    suspend fun deleteTodo(todo: TodoItem) {
        dao.delete(todo)
    }
}

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    private val _todos = mutableStateListOf<TodoItem>()
    val allTodos: List<TodoItem> get() = _todos
    val activeTodos: List<TodoItem> get() = _todos.filter { !it.done }
    val completedTodos: List<TodoItem> get() = _todos.filter { it.done }

    init {
        repository.todos.observeForever { items ->
            _todos.clear()
            _todos.addAll(items)
        }
    }

    fun addTodo(task: String) = viewModelScope.launch {
        val newTodo = TodoItem(task = task)
        repository.addTodo(newTodo)
        _todos.add(newTodo)
    }

    fun toggleTodoStatus(todo: TodoItem) = viewModelScope.launch {
        val updatedTodo = todo.copy(done = !todo.done)
        repository.updateTodo(updatedTodo)
        val index = _todos.indexOf(todo)
        if (index != -1) {
            _todos[index] = updatedTodo
        }
    }


    fun deleteTodo(todo: TodoItem) = viewModelScope.launch {
        repository.deleteTodo(todo)
        _todos.remove(todo)
    }
    fun updateTodo(todo: TodoItem) = viewModelScope.launch {
        repository.updateTodo(todo)
        // Optional: Update the item in the local list to reflect changes immediately in the UI
        _todos.find { it.id == todo.id }?.let {
            val index = _todos.indexOf(it)
            _todos[index] = todo
        }
    }

}

@Composable
fun TodoList(todos: List<TodoItem>, todoViewModel: TodoViewModel) {
    LazyColumn {
        items(todos) { todo ->
            TodoRow(todo, todoViewModel)
        }
    }
}
@Composable
fun TodoRow(todo: TodoItem, todoViewModel: TodoViewModel) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(todo.task) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isEditing) {
            TextField(
                value = editText,
                onValueChange = { editText = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (editText.isNotBlank()) {
                        todoViewModel.updateTodo(todo.copy(task = editText))
                        isEditing = false
                    }
                })
            )
        } else {
            Text(todo.task, Modifier.weight(1f))
            Checkbox(
                checked = todo.done,
                onCheckedChange = { todoViewModel.toggleTodoStatus(todo) }
            )
        }
        IconButton(onClick = { isEditing = !isEditing }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
        }
        IconButton(onClick = { todoViewModel.deleteTodo(todo) }) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@SuppressLint("RememberReturnType")
@Composable
fun TodoScreen(nav: NavController) {
    val todoViewModel: TodoViewModel = viewModel(
        factory = TodoViewModelFactory(
            TodoRepository(
                DatabaseBuilder.getInstance(LocalContext.current).todoDao()
            )
        )
    )
    val context = LocalContext.current
    BackHandler {
        (context as? Activity)?.finish()
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Active", "Completed")
    var text by remember { mutableStateOf("") }


    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            placeholder = { Text("Add a new task") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (text.isNotBlank()) {
                    todoViewModel.addTodo(text)
                    text = ""
                }
            })
        )
        when (selectedTabIndex) {
            0 -> TodoList(todos = todoViewModel.allTodos, todoViewModel)
            1 -> TodoList(todos = todoViewModel.activeTodos, todoViewModel)
            2 -> TodoList(todos = todoViewModel.completedTodos, todoViewModel)
        }
    }
}