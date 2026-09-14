package com.pocket.app.ui.vault

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.model.PocketItem
import com.pocket.app.ui.preview.FilePreviewDialog
import com.pocket.app.ui.viewmodel.PocketViewModel
import com.pocket.app.utils.FileUtils
import com.pocket.app.utils.LanguageHelper
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    title: String,
    targetType: ItemType,
    viewModel: PocketViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()

    val itemsFlow = if (targetType == ItemType.PHOTO) viewModel.photos else viewModel.documents
    val items by itemsFlow.collectAsState()

    val dbFoldersFlow = if (targetType == ItemType.PHOTO) viewModel.photoFolders else viewModel.docFolders
    val dbFolders by dbFoldersFlow.collectAsState()

    var customFolders by remember { mutableStateOf<List<String>>(emptyList()) }
    val allFolders = remember(dbFolders, customFolders) {
        (dbFolders + customFolders).filter { it.isNotBlank() && it != "General" }.distinct()
    }

    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<PocketItem?>(null) }
    var itemToMove by remember { mutableStateOf<PocketItem?>(null) }
    var itemToRename by remember { mutableStateOf<PocketItem?>(null) }
    var previewItem by remember { mutableStateOf<PocketItem?>(null) }
    var showNewFolderDialog by remember { mutableStateOf(false) }

    // Direct File Picker: Picks and instantly saves without annoying popup
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.saveIncomingFile(
                uri = it,
                title = "",
                category = ItemCategory.GENERAL,
                folderName = selectedFolder ?: "General"
            ) {
                Toast.makeText(
                    context,
                    LanguageHelper.text(language, "Saved to Pocket!", "பாக்கெட்டில் சேமிக்கப்பட்டது!"),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val filteredItems = items.filter { item ->
        selectedFolder == null || item.folderName == selectedFolder
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            if (targetType == ItemType.PHOTO)
                                LanguageHelper.text(language, "Photos & Gallery", "படங்கள் & தொகுப்பு")
                            else
                                LanguageHelper.text(language, "Documents & PDF", "ஆவணங்கள் & PDF"),
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
                onClick = {
                    val mime = if (targetType == ItemType.PHOTO) "image/*" else "*/*"
                    filePicker.launch(mime)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (targetType == ItemType.PHOTO)
                        LanguageHelper.text(language, "Add Photo", "படம் சேர்")
                    else
                        LanguageHelper.text(language, "Upload File", "கோப்பு சேர்"),
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
            // Optional Folder Filter Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        LanguageHelper.text(language, "📁 Folders", "📁 ஃபோல்டர்கள்"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            LanguageHelper.text(language, "+ New Folder", "+ புதிய ஃபோல்டர்"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                // Horizontal Folder Bar (All + user folders)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "All" Pill
                    item {
                        FolderBadgeCard(
                            name = LanguageHelper.text(language, "All", "அனைத்தும்"),
                            count = items.size,
                            isSelected = selectedFolder == null,
                            onClick = { selectedFolder = null }
                        )
                    }

                    // User Created Folder Pills
                    items(allFolders) { folderName ->
                        val count = items.count { it.folderName == folderName }
                        FolderBadgeCard(
                            name = folderName,
                            count = count,
                            isSelected = selectedFolder == folderName,
                            onClick = { selectedFolder = folderName }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Files List or Empty State
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            if (targetType == ItemType.PHOTO) Icons.Default.PhotoLibrary else Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (selectedFolder != null)
                                LanguageHelper.text(
                                    language,
                                    "No files in folder '$selectedFolder'.",
                                    "'$selectedFolder' ஃபோல்டரில் கோப்புகள் இல்லை."
                                )
                            else
                                LanguageHelper.text(
                                    language,
                                    "No files found here.",
                                    "இங்கு கோப்புகள் எதுவும் இல்லை."
                                ),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            LanguageHelper.text(
                                language,
                                "Tap the + button below to add files.",
                                "கீழே உள்ள + பட்டனை அழுத்தி கோப்புகளை சேர்க்கலாம்."
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        VaultItemCard(
                            item = item,
                            onPreview = { previewItem = item },
                            onRename = { itemToRename = item },
                            onShare = { viewModel.shareItem(item) },
                            onTogglePin = { viewModel.togglePin(item) },
                            onMove = { itemToMove = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // In-App File Preview Dialog
    previewItem?.let { item ->
        FilePreviewDialog(
            item = item,
            language = language,
            onDismiss = { previewItem = null },
            onOpenExternal = {
                item.filePath?.let { FileUtils.openFile(context, it) }
            },
            onShare = { viewModel.shareItem(item) },
            onDelete = {
                itemToDelete = item
                previewItem = null
            },
            onRename = { newTitle ->
                viewModel.renameItem(item, newTitle) {
                    Toast.makeText(
                        context,
                        LanguageHelper.text(language, "Renamed successfully! ✓", "பெயர் மாற்றப்பட்டது! ✓"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    // Rename Item Dialog
    itemToRename?.let { item ->
        var renameText by remember { mutableStateOf(item.title) }
        AlertDialog(
            onDismissRequest = { itemToRename = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        LanguageHelper.text(language, "Rename File", "கோப்பின் பெயரை மாற்று"),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        LanguageHelper.text(language, "Enter new file name:", "புதிய பெயரை உள்ளிடவும்:"),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it },
                        label = {
                            Text(LanguageHelper.text(language, "File Name", "கோப்பு பெயர்"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (renameText.isNotEmpty()) {
                                IconButton(onClick = { renameText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameText.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.renameItem(item, trimmed) {
                                Toast.makeText(
                                    context,
                                    LanguageHelper.text(language, "Renamed successfully! ✓", "பெயர் மாற்றப்பட்டது! ✓"),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            itemToRename = null
                        }
                    }
                ) {
                    Text(LanguageHelper.text(language, "Save", "சேமி"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToRename = null }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }

    // Create New Folder Dialog
    if (showNewFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Text(
                    LanguageHelper.text(language, "New Folder", "புதிய ஃபோல்டர்"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        LanguageHelper.text(language, "Enter folder name:", "ஃபோல்டரின் பெயரை தட்டச்சு செய்க:")
                    )
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = {
                            Text(LanguageHelper.text(language, "Folder Name", "ஃபோல்டர் பெயர்"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newFolderName.trim()
                        if (trimmed.isNotBlank()) {
                            customFolders = (customFolders + trimmed).distinct()
                            selectedFolder = trimmed
                        }
                        showNewFolderDialog = false
                    },
                    enabled = newFolderName.isNotBlank()
                ) {
                    Text(LanguageHelper.text(language, "Create", "உருவாக்கு"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }

    // Move to Another Folder Dialog
    itemToMove?.let { item ->
        var targetFolder by remember { mutableStateOf(item.folderName) }
        var customNewFolder by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { itemToMove = null },
            title = {
                Text(
                    LanguageHelper.text(language, "Move to Folder", "ஃபோல்டருக்கு மாற்று"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        LanguageHelper.text(
                            language,
                            "Select folder for '${item.title}':",
                            "'${item.title}' கோப்பை எந்த ஃபோல்டருக்கு மாற்ற வேண்டும்?"
                        )
                    )
                    Column(modifier = Modifier.heightIn(max = 180.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetFolder = "General" }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetFolder == "General",
                                onClick = { targetFolder = "General" }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                LanguageHelper.text(language, "General (No folder)", "பொது (ஃபோல்டர் இல்லை)"),
                                fontSize = 14.sp
                            )
                        }

                        allFolders.forEach { folder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { targetFolder = folder }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = targetFolder == folder,
                                    onClick = { targetFolder = folder }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder, fontSize = 14.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customNewFolder,
                        onValueChange = { customNewFolder = it },
                        label = {
                            Text(LanguageHelper.text(language, "Or type new folder name", "அல்லது புதிய பெயர்"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalFolder = if (customNewFolder.isNotBlank()) {
                            customFolders = (customFolders + customNewFolder.trim()).distinct()
                            customNewFolder.trim()
                        } else {
                            targetFolder
                        }
                        viewModel.updateFolder(item, finalFolder)
                        itemToMove = null
                    }
                ) {
                    Text(LanguageHelper.text(language, "Move", "மாற்று"))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToMove = null }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = {
                Text(
                    LanguageHelper.text(language, "Delete File?", "கோப்பை நீக்கவா?"),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    LanguageHelper.text(
                        language,
                        "Are you sure you want to delete '${item.title}'?",
                        "'${item.title}' கோப்பை நிச்சயமாக நீக்க வேண்டுமா?"
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item)
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

@Composable
fun FolderBadgeCard(
    name: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (name.contains("All") || name.contains("அனைத்தும்")) "📂" else "📁",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.4f)
            ) {
                Text(
                    count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun VaultItemCard(
    item: PocketItem,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onRename: (() -> Unit)? = null
) {
    val file = remember(item.filePath) { item.filePath?.let { File(it) } }
    val isPdf = remember(item) {
        item.fileExtension.equals("pdf", ignoreCase = true) ||
                (file != null && file.name.endsWith(".pdf", ignoreCase = true))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreview() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Section: Thumbnail + Title + Info + Pin Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Squircle Thumbnail or Styled Badge
                if (item.itemType == ItemType.PHOTO && file != null && file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.LightGray.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                if (isPdf) Color(0xFFFEE2E2)
                                else if (item.itemType == ItemType.PHOTO) Color(0xFFDBEAFE)
                                else Color(0xFFD1FAE5),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPdf) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "PDF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFDC2626)
                                )
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            Icon(
                                if (item.itemType == ItemType.PHOTO) Icons.Default.PhotoLibrary else Icons.Default.Description,
                                contentDescription = null,
                                tint = if (item.itemType == ItemType.PHOTO) Color(0xFF2563EB) else Color(0xFF059669),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.folderName.isNotBlank() && item.folderName != "General") {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    "📁 ${item.folderName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            FileUtils.formatFileSize(item.fileSize),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (item.isPinned) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Pinned",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Section: Modern Soft-Tinted Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Preview / View button
                ActionBadgeButton(
                    icon = Icons.Default.Visibility,
                    contentDescription = "Preview",
                    containerColor = Color(0xFFEFF6FF),
                    contentColor = Color(0xFF2563EB),
                    onClick = onPreview
                )

                // Rename button
                if (onRename != null) {
                    ActionBadgeButton(
                        icon = Icons.Default.DriveFileRenameOutline,
                        contentDescription = "Rename",
                        containerColor = Color(0xFFEEF2FF),
                        contentColor = Color(0xFF6366F1),
                        onClick = onRename
                    )
                }

                // Move Folder button
                ActionBadgeButton(
                    icon = Icons.Default.DriveFileMove,
                    contentDescription = "Move to Folder",
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF64748B),
                    onClick = onMove
                )

                // Pin button
                ActionBadgeButton(
                    icon = Icons.Default.Star,
                    contentDescription = "Pin",
                    containerColor = if (item.isPinned) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
                    contentColor = if (item.isPinned) Color(0xFFD97706) else Color.LightGray,
                    onClick = onTogglePin
                )

                // WhatsApp Share button
                ActionBadgeButton(
                    icon = Icons.Default.Share,
                    contentDescription = "Share",
                    containerColor = Color(0xFFDCFCE7),
                    contentColor = Color(0xFF16A34A),
                    onClick = onShare
                )

                // Delete button
                ActionBadgeButton(
                    icon = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = Color(0xFFDC2626),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun ActionBadgeButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
