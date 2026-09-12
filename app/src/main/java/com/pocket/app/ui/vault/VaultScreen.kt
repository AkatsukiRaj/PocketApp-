package com.pocket.app.ui.vault

import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket.app.data.model.ItemCategory
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.model.PocketItem
import com.pocket.app.ui.viewmodel.PocketViewModel
import com.pocket.app.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    title: String,
    targetType: ItemType,
    viewModel: PocketViewModel,
    onBack: () -> Unit
) {
    val itemsFlow = if (targetType == ItemType.PHOTO) viewModel.photos else viewModel.documents
    val items by itemsFlow.collectAsState()

    val dbFoldersFlow = if (targetType == ItemType.PHOTO) viewModel.photoFolders else viewModel.docFolders
    val dbFolders by dbFoldersFlow.collectAsState()

    val defaultPresets = remember(targetType) {
        if (targetType == ItemType.PHOTO) {
            listOf("Prescriptions (மருந்துச் சீட்டு)", "Bills (ரசீதுகள்)", "ID Cards (அடையாள அட்டை)", "Family (குடும்பம்)", "General (பொது)")
        } else {
            listOf("Medical Reports (மருத்துவ அறிக்கை)", "Bank & Finance (வங்கி)", "Property (பத்திரம்)", "Certificates (சான்றிதழ்)", "General (பொது)")
        }
    }

    var customFolders by remember { mutableStateOf<List<String>>(emptyList()) }
    val allFolders = remember(defaultPresets, dbFolders, customFolders) {
        (defaultPresets + dbFolders + customFolders).distinct()
    }

    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<PocketItem?>(null) }
    var itemToMove by remember { mutableStateOf<PocketItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            showAddDialog = true
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
                            if (targetType == ItemType.PHOTO) "ஆல்பங்கள் & படங்கள் (Albums & Photos)" else "கோப்புகள் & ஆவணங்கள் (Folders & Docs)",
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
                    if (targetType == ItemType.PHOTO) "Add Photo (படம் சேர்)" else "Upload File (கோப்பு சேர்)",
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
            // Album / Folder Header Section
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
                        if (targetType == ItemType.PHOTO) "📁 ஆல்பங்கள் (Albums)" else "📁 கோப்புறைகள் (Folders)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = { showNewFolderDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("புதிய ஆல்பம் (+ New)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Horizontal Album / Folder Cards
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // All Items Card
                    item {
                        FolderBadgeCard(
                            name = "அனைத்தும் (All)",
                            count = items.size,
                            isSelected = selectedFolder == null,
                            onClick = { selectedFolder = null }
                        )
                    }

                    // Individual Folder Cards
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

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

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
                            if (targetType == ItemType.PHOTO) Icons.Default.AccountBox else Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            if (selectedFolder != null) "\"$selectedFolder\" ஆல்பத்தில் கோப்புகள் இல்லை." else "இங்கு கோப்புகள் எதுவும் இல்லை.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "கீழே உள்ள + பட்டனை அழுத்தி கோப்புகளை சேர்க்கலாம்.",
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

    // Create New Folder / Album Dialog
    if (showNewFolderDialog) {
        var newFolderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = {
                Text(
                    if (targetType == ItemType.PHOTO) "புதிய ஆல்பம் (New Album)" else "புதிய கோப்புறை (New Folder)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ஆல்பத்தின் பெயரை தட்டச்சு செய்க:")
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("ஆல்பம் பெயர் (Album Name)") },
                        modifier = Modifier.fillMaxWidth()
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
                    Text("உருவாக்கு (Create)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("ரத்து (Cancel)")
                }
            }
        )
    }

    // Save File Dialog with Album Selector
    if (showAddDialog && selectedUri != null) {
        var inputTitle by remember { mutableStateOf("") }
        var chosenFolder by remember { mutableStateOf(selectedFolder ?: allFolders.firstOrNull() ?: "General") }
        var chosenCategory by remember { mutableStateOf(ItemCategory.MEDICAL) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save to Pocket (சேமிக்க)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("பெயர் (File Name)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        if (targetType == ItemType.PHOTO) "சேமிக்க வேண்டிய ஆல்பம் (Album):" else "சேமிக்க வேண்டிய கோப்புறை (Folder):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )

                    // Scrollable list of folders to pick
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                    ) {
                        allFolders.forEach { folder ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { chosenFolder = folder }
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = chosenFolder == folder,
                                    onClick = { chosenFolder = folder }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(folder, fontSize = 14.sp, fontWeight = if (chosenFolder == folder) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUri?.let { uri ->
                            viewModel.saveIncomingFile(
                                uri = uri,
                                title = inputTitle,
                                category = chosenCategory,
                                folderName = chosenFolder
                            )
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Save (சேமி)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel (ரத்து)")
                }
            }
        )
    }

    // Move to Another Album Dialog
    itemToMove?.let { item ->
        var targetFolder by remember { mutableStateOf(item.folderName) }
        AlertDialog(
            onDismissRequest = { itemToMove = null },
            title = { Text("ஆல்பம் மாற்றுக (Move to Album)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("\"${item.title}\" கோப்பை எந்த ஆல்பத்திற்கு மாற்ற வேண்டும்?")
                    Column(modifier = Modifier.heightIn(max = 200.dp)) {
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
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateFolder(item, targetFolder)
                        itemToMove = null
                    }
                ) {
                    Text("மாற்று (Move)")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToMove = null }) {
                    Text("ரத்து (Cancel)")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("நீக்க வேண்டுமா? (Delete)", fontWeight = FontWeight.Bold) },
            text = { Text("\"${item.title}\" கோப்பை நிச்சயமாக நீக்க வேண்டுமா?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("நீக்கு (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("ரத்து (Cancel)")
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
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
            shape = RoundedCornerShape(16.dp)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                "📁",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$count items",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun VaultItemCard(
    item: PocketItem,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (item.itemType == ItemType.PHOTO) Color(0xFFDBEAFE) else Color(0xFFD1FAE5),
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (item.itemType == ItemType.PHOTO) Icons.Default.AccountBox else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (item.itemType == ItemType.PHOTO) Color(0xFF2563EB) else Color(0xFF059669),
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                    Text(
                        "📁 ${item.folderName} • ${FileUtils.formatFileSize(item.fileSize)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Move / Folder button
                IconButton(onClick = onMove) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Move to Album",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Pin button
                IconButton(onClick = onTogglePin) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Pin",
                        tint = if (item.isPinned) Color(0xFFF59E0B) else Color.LightGray
                    )
                }
                // Share button
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}
