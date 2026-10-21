package com.example.lab08

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.example.lab08.ui.theme.Lab08Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Lab08Theme {
                val db = remember {
                    Room.databaseBuilder(
                        applicationContext,
                        TaskDatabase::class.java,
                        "task_db"
                    ).build()
                }
                val viewModel = remember { TaskViewModel(db.taskDao()) }

                TaskScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis tareas") },
                actions = {
                    TextButton(onClick = { viewModel.deleteAllTasks() }) {
                        Text("Borrar todo")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar tarea")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(tasks, key = { it.id }) { task ->
                TaskRow(
                    task = task,
                    onToggle = { viewModel.toggleTaskCompletion(task) },
                    onEdit = { taskToEdit = task },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }
    }

    if (showAddDialog) {
        TaskDialog(
            title = "Nueva tarea",
            initialText = "",
            onConfirm = {
                viewModel.addTask(it)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    taskToEdit?.let { task ->
        TaskDialog(
            title = "Editar tarea",
            initialText = task.description,
            onConfirm = {
                viewModel.editTask(task, it)
                taskToEdit = null
            },
            onDismiss = { taskToEdit = null }
        )
    }
}

@Composable
fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo de completado, como en Google Tasks
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .then(
                    if (task.isCompleted)
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    else
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (task.isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completada",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = task.description,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            color = if (task.isCompleted)
                MaterialTheme.colorScheme.outline
            else
                MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, contentDescription = "Editar tarea")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar tarea")
        }
    }
}

@Composable
fun TaskDialog(
    title: String,
    initialText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Descripción") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (text.isNotBlank()) onConfirm(text.trim()) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}