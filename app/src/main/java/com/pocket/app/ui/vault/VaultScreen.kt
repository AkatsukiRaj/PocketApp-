package com.pocket.app.ui.vault

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

    var selectedCategory by remember { mutableStateOf<ItemCategory?>(null) }
    var itemToDelete by remember { mutableStateOf<PocketItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            showAddDialog = true
        }
    }

    val filteredItems = items.filter {
        selectedCategory == null || it.category == selectedCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            if (targetType == ItemType.PHOTO) "புகைப்படங்கள் & ஆல்பம்" else "ஆவணங்கள் (PDF / Excel)",
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
            // Category Filter Badges
            ScrollableTabRow(
                selectedTabIndex = selectedCategory?.ordinal?.plus(1) ?: 0,
                edgePadding = 16.dp,
                divider = {}
            ) {
                Tab(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    text = { Text("அனைத்தும் (All)", fontWeight = FontWeight.Bold) }
                )
                ItemCategory.values().forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = { Text("${cat.displayName} (${cat.tamilName})", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

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
                            if (targetType == ItemType.PHOTO) Icons.Default.PhotoLibrary else Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "இங்கு கோப்புகள் எதுவும் இல்லை.",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
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
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Save File Dialog
    if (showAddDialog && selectedUri != null) {
        var inputTitle by remember { mutableStateOf("") }
        var chosenCategory by remember { mutableStateOf(ItemCategory.MEDICAL) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save to Pocket (சேமிக்க)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("பெயர் (File Name)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("பிரிவை தேர்வு செய்க:", fontWeight = FontWeight.SemiBold)
                    ItemCategory.values().forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { chosenCategory = cat }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = chosenCategory == cat, onClick = { chosenCategory = cat })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${cat.displayName} (${cat.tamilName})")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUri?.let { uri ->
                            viewModel.saveIncomingFile(uri, inputTitle, chosenCategory)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("Save (சேமி)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("நீக்க வேண்டுமா? (Delete)") },
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
fun VaultItemCard(
    item: PocketItem,
    onShare: () -> Unit,
    onTogglePin: () -> Unit,
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
                        if (item.itemType == ItemType.PHOTO) Icons.Default.Image else Icons.Default.Description,
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
                        "${item.category.tamilName} • ${FileUtils.formatFileSize(item.fileSize)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pin button
                IconButton(onClick = onTogglePin) {
                    Icon(
                        if (item.isPinned) Icons.Default.PushPin else Icons.Default.OutlinedFlag,
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
