package com.pocket.app.ui.preview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.model.PocketItem
import com.pocket.app.data.preferences.AppLanguage
import com.pocket.app.utils.FileUtils
import com.pocket.app.utils.LanguageHelper
import com.pocket.app.utils.PdfUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FilePreviewDialog(
    item: PocketItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onOpenExternal: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onRename: ((String) -> Unit)? = null
) {
    val file = remember(item.filePath) { item.filePath?.let { File(it) } }
    val isPdf = remember(item) {
        item.fileExtension.equals("pdf", ignoreCase = true) ||
                (file != null && file.name.endsWith(".pdf", ignoreCase = true))
    }

    var currentTitle by remember(item.title) { mutableStateOf(item.title) }
    var showRenameDialog by remember { mutableStateOf(false) }

    var pdfPages by remember { mutableStateOf<List<Bitmap>?>(null) }
    var isLoadingPdf by remember { mutableStateOf(false) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(item.id) {
        currentTitle = item.title
        scale = 1f
        offset = Offset.Zero
        if (isPdf && file != null && file.exists()) {
            isLoadingPdf = true
            pdfPages = PdfUtils.renderPages(file)
            isLoadingPdf = false
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val formattedDate = remember(item.createdAt) { dateFormat.format(Date(item.createdAt)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar with Editable Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showRenameDialog = true }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Rename",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "📁 ${item.folderName} • ${FileUtils.formatFileSize(item.fileSize)} • $formattedDate",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Preview Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(if (item.itemType == ItemType.PHOTO) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.itemType == ItemType.PHOTO && file != null && file.exists()) {
                        // Photo Preview with Pinch-to-Zoom, Double-Tap, Pan & Buttons
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .pointerInput(item.id) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (scale * zoom).coerceIn(1f, 5f)
                                        scale = newScale
                                        if (newScale > 1f) {
                                            val maxOffsetX = (size.width * (newScale - 1f)) / 2f
                                            val maxOffsetY = (size.height * (newScale - 1f)) / 2f
                                            offset = Offset(
                                                x = (offset.x + pan.x * newScale).coerceIn(-maxOffsetX, maxOffsetX),
                                                y = (offset.y + pan.y * newScale).coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        } else {
                                            offset = Offset.Zero
                                        }
                                    }
                                }
                                .pointerInput(item.id) {
                                    detectTapGestures(
                                        onDoubleTap = {
                                            if (scale > 1f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            } else {
                                                scale = 2.5f
                                            }
                                        }
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = currentTitle,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    ),
                                contentScale = ContentScale.Fit
                            )

                            // Floating Zoom Controls Pill at Bottom Right
                            Surface(
                                color = Color.Black.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Zoom Out (-)
                                    IconButton(
                                        onClick = {
                                            val newScale = (scale - 0.5f).coerceIn(1f, 5f)
                                            scale = newScale
                                            if (newScale <= 1f) {
                                                scale = 1f
                                                offset = Offset.Zero
                                            }
                                        },
                                        modifier = Modifier.size(36.dp),
                                        enabled = scale > 1f
                                    ) {
                                        Icon(
                                            Icons.Default.Remove,
                                            contentDescription = "Zoom Out",
                                            tint = if (scale > 1f) Color.White else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Zoom Level Indicator
                                    Text(
                                        text = String.format(Locale.US, "%.1fx", scale),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    // Zoom In (+)
                                    IconButton(
                                        onClick = {
                                            val newScale = (scale + 0.5f).coerceIn(1f, 5f)
                                            scale = newScale
                                        },
                                        modifier = Modifier.size(36.dp),
                                        enabled = scale < 5f
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = "Zoom In",
                                            tint = if (scale < 5f) Color.White else Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    // Reset Zoom Button
                                    if (scale > 1f) {
                                        IconButton(
                                            onClick = {
                                                scale = 1f
                                                offset = Offset.Zero
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Reset Zoom",
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Gentle Zoom Hint Badge
                            if (scale == 1f) {
                                Surface(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        LanguageHelper.text(
                                            language,
                                            "🔍 Pinch or double-tap to zoom",
                                            "🔍 பெரிதாக்க இருமுறை தொடவும் அல்லது பிஞ்ச் செய்யவும்"
                                        ),
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else if (isPdf) {
                        // PDF Preview
                        if (isLoadingPdf) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    LanguageHelper.text(language, "Loading PDF pages...", "PDF பக்கங்கள் தயாராகிறது..."),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        } else if (!pdfPages.isNullOrEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                itemsIndexed(pdfPages!!) { index, pageBitmap ->
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Image(
                                                bitmap = pageBitmap.asImageBitmap(),
                                                contentDescription = "Page ${index + 1}",
                                                modifier = Modifier.fillMaxWidth(),
                                                contentScale = ContentScale.FillWidth
                                            )
                                            Surface(
                                                color = Color.Black.copy(alpha = 0.05f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    "${LanguageHelper.text(language, "Page", "பக்கம்")} ${index + 1} / ${pdfPages!!.size}",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray,
                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Fallback for PDF
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(
                                    color = Color(0xFFFEE2E2),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("PDF", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFFDC2626))
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(currentTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    LanguageHelper.text(language, "Tap below to view in full PDF app", "கீழே உள்ள பட்டனை அழுத்தி PDF ஆப்பில் திறக்கலாம்"),
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        // Generic Document Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Surface(
                                color = Color(0xFFE0E7FF),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        item.fileExtension?.uppercase()?.ifBlank { "DOC" } ?: "DOC",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = Color(0xFF4338CA)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(currentTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                LanguageHelper.text(language, "Document file ready", "ஆவணம் தயாராக உள்ளது"),
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Action Buttons Bar (Rename, Open App, Share, Delete)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rename Button
                    Button(
                        onClick = { showRenameDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            LanguageHelper.text(language, "Rename", "பெயர் மாற்று"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Open in Full App Button
                    Button(
                        onClick = onOpenExternal,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            LanguageHelper.text(language, "Open App", "வெளியில் திற"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Share Button (WhatsApp)
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            LanguageHelper.text(language, "Share", "பகிர்க"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .background(Color(0xFFFEE2E2), RoundedCornerShape(12.dp))
                            .size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    if (showRenameDialog) {
        var editTitleText by remember { mutableStateOf(currentTitle) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        LanguageHelper.text(language, "Rename File", "கோப்பின் பெயரை மாற்று"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
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
                        value = editTitleText,
                        onValueChange = { editTitleText = it },
                        label = {
                            Text(LanguageHelper.text(language, "File Name", "கோப்பு பெயர்"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (editTitleText.isNotEmpty()) {
                                IconButton(onClick = { editTitleText = "" }) {
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
                        val trimmed = editTitleText.trim()
                        if (trimmed.isNotBlank()) {
                            currentTitle = trimmed
                            onRename?.invoke(trimmed)
                            showRenameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(LanguageHelper.text(language, "Save", "சேமி"), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text(LanguageHelper.text(language, "Cancel", "ரத்து"))
                }
            }
        )
    }
}
