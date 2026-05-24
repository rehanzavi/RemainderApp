package com.rpzsoftware.remindly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rpzsoftware.remindly.model.Note
import com.rpzsoftware.remindly.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemindlyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF5F7FA)
                ) {
                    MainNavigation()
                }
            }
        }
    }
}

@Composable
fun RemindlyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4A90E2),
            onPrimary = Color.White,
            surface = Color.White,
            background = Color(0xFFF5F7FA),
            surfaceVariant = Color(0xFFF0F4F8)
        ),
        content = content
    )
}

@Composable
fun MainNavigation(viewModel: NoteViewModel = viewModel()) {
    val navController = rememberNavController()
    val userName by viewModel.userName.collectAsState()

    NavHost(navController = navController, startDestination = "check_auth") {
        composable("check_auth") {
            LaunchedEffect(userName) {
                if (userName == null) {
                    navController.navigate("welcome") {
                        popUpTo("check_auth") { inclusive = true }
                    }
                } else {
                    navController.navigate("notes") {
                        popUpTo("check_auth") { inclusive = true }
                    }
                }
            }
        }
        composable("welcome") {
            WelcomeScreen(onNameEntered = { name ->
                viewModel.saveUserName(name)
                navController.navigate("notes") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }
        composable("notes") {
            NotesScreen(
                userName = userName ?: "",
                viewModel = viewModel,
                onNoteClick = { note ->
                    navController.navigate("edit_note/${note.id}")
                },
                onAddNote = {
                    navController.navigate("edit_note/-1")
                }
            )
        }
        composable(
            route = "edit_note/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            EditNoteScreen(
                noteId = noteId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.6f),
            tint = Color.White
        )
    }
}

@Composable
fun WelcomeScreen(onNameEntered: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(modifier = Modifier.size(120.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Welcome to Remindly",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Stay organized and productive. What should we call you?",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Enter your name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { if (name.isNotBlank()) onNameEntered(name) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = name.isNotBlank()
        ) {
            Text("Get Started", fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    userName: String,
    viewModel: NoteViewModel,
    onNoteClick: (Note) -> Unit,
    onAddNote: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf("All") }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, null) },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Search, null) },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.White)
                        }
                    },
                    selected = false,
                    onClick = onAddNote,
                    label = null,
                    alwaysShowLabel = false
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Notifications, null) },
                    selected = false,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, null) },
                    selected = false,
                    onClick = {}
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            HeaderSection(userName)
            Spacer(modifier = Modifier.height(24.dp))
            CategoryTabs(selectedCategory) { selectedCategory = it }
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(notes) { note ->
                    NoteItem(
                        note = note,
                        onClick = { onNoteClick(note) },
                        onDelete = { noteToDelete = note }
                    )
                }
            }
        }
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        noteToDelete?.let { viewModel.delete(it) }
                        noteToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    noteId: Int,
    viewModel: NoteViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState(initial = emptyList())
    val existingNote = remember(noteId, notes) { notes.find { it.id == noteId } }

    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var content by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == -1) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (title.isNotBlank()) {
                            if (noteId == -1) {
                                viewModel.insert(Note(title = title, content = content))
                            } else {
                                val existingNote = notes.find { it.id == noteId }
                                if (existingNote != null) {
                                    viewModel.update(existingNote.copy(title = title, content = content))
                                }
                            }
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Type something...") },
                modifier = Modifier.fillMaxSize(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun HeaderSection(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning,",
                fontSize = 16.sp,
                color = Color.Gray
            )
            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun CategoryTabs(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("All", "Notes", "Tasks", "Reminders")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onCategorySelected(category) },
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                shape = RoundedCornerShape(24.dp),
                tonalElevation = if (isSelected) 0.dp else 2.dp
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    color = if (isSelected) Color.White else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOTES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1A1C1E)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = note.content,
                fontSize = 14.sp,
                color = Color(0xFF43474E),
                maxLines = 3,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(Date(note.timestamp)),
                fontSize = 12.sp,
                color = Color.LightGray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun EditNotePreview() {
    RemindlyTheme {
        EditNoteScreen(noteId = -1, viewModel = viewModel(), onBack = {})
    }
}
