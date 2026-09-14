package com.pocket.app.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket.app.data.model.PocketItem
import com.pocket.app.ui.viewmodel.PocketViewModel
import com.pocket.app.utils.FileUtils
import com.pocket.app.utils.LanguageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: PocketViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val language by viewModel.language.collectAsState()
    val context = LocalContext.current
    var showCreateDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<PocketItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            LanguageHelper.text(language, "Smart Notes", "குறிப்புகள்"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            LanguageHelper.text(language, "Quick notes & checklists", "குறிப்புகள் & நினைவுக் குறிப்புகள்"),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.StickyNote2, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    LanguageHelper.text(language, "New Note", "புது குறிப்பு"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            LanguageHelper.text(language, "No notes yet.", "குறிப்புகள் எதுவும் இல்லை."),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            LanguageHelper.text(
                                language,
                                "Tap + to write a grocery list or note.",
                                "+ பட்டனை அழுத்தி மளிகை பட்டியல் அல்லது குறிப்பை எழுதலாம்."
                            ),
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(notes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(
                                1.dp,
                                Color(0xFF8B5CF6).copy(alpha = 0.25f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFF3E8FF),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.StickyNote2,
                                                    contentDescription = null,
                                                    tint = Color(0xFF7C3AED),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            note.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            maxLines = 1
                                        )
                                    }

                                    Surface(
                                        onClick = { viewModel.togglePin(note) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (note.isPinned) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "Pin",
                                                tint = if (note.isPinned) Color(0xFFD97706) else Color.LightGray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    note.description,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // WhatsApp Share button
                                    Surface(
                                        onClick = {
                                            FileUtils.shareText(
                                                context,
                                                "${note.title}\n\n${note.description}",
                                                note.title
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFDCFCE7),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Share,
                                                contentDescription = "Share",
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Delete button
                                    Surface(
                                        onClick = { itemToDelete = note },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEE2E2),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Note Dialog
    if (showCreateDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    LanguageHelper.text(language, "New Note", "புது குறிப்பு"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = {
                            Text(LanguageHelper.text(language, "Title", "தலைப்பு"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = {
                            Text(LanguageHelper.text(language, "Description", "விவரம்"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteContent)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text(LanguageHelper.text(language, "Save", "சேமி"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }

    // Delete confirmation
    itemToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    LanguageHelper.text(language, "Delete Note?", "குறிப்பை நீக்கவா?"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    LanguageHelper.text(
                        language,
                        "Are you sure you want to delete '${note.title}'?",
                        "'${note.title}' குறிப்பை நிச்சயமாக நீக்க வேண்டுமா?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(note)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(LanguageHelper.text(language, "Delete", "நீக்கு"))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }
}
