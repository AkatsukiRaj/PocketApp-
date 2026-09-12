package com.pocket.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket.app.data.model.PocketItem
import com.pocket.app.data.preferences.AppLanguage
import com.pocket.app.ui.settings.SettingsDialog
import com.pocket.app.ui.viewmodel.PocketViewModel
import com.pocket.app.utils.LanguageHelper

@Composable
fun HomeScreen(
    viewModel: PocketViewModel,
    onNavigateToPhotos: () -> Unit,
    onNavigateToDocs: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onOpenItem: (PocketItem) -> Unit
) {
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val pinnedItems by viewModel.pinnedItems.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val docs by viewModel.documents.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            currentLanguage = language,
            currentThemeMode = themeMode,
            onLanguageChange = { viewModel.setLanguage(it) },
            onThemeChange = { viewModel.setThemeMode(it) },
            onDismiss = { showSettings = false }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App Header with Settings Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFF59E0B), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("P", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Pocket",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White
                                )
                                if (language == AppLanguage.BOTH) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "பாக்கெட்",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                LanguageHelper.text(
                                    language,
                                    "Personal Digital Vault",
                                    "உங்கள் தனிப்பட்ட பெட்டகம்"
                                ),
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Settings Button
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            .size(42.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Pinned Shortcuts Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            LanguageHelper.text(
                                language,
                                "Quick Shortcuts",
                                "முக்கிய குறுக்குவழிகள்"
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Home Screen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (pinnedItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            LanguageHelper.text(
                                language,
                                "Pin important documents or notes to access them quickly here.",
                                "முக்கிய ஆவணங்களை Pin செய்து இங்கு உடனே பார்க்கலாம்."
                            ),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pinnedItems) { item ->
                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { onOpenItem(item) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        item.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        maxLines = 1
                                    )
                                    val catLabel = when (language) {
                                        AppLanguage.ENGLISH -> item.category.displayName
                                        AppLanguage.TAMIL -> item.category.tamilName
                                        AppLanguage.BOTH -> "${item.category.displayName} (${item.category.tamilName})"
                                    }
                                    Text(
                                        catLabel,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4 Categories
        item {
            Text(
                LanguageHelper.text(language, "Categories", "பிரிவுகள்"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            MainCategoryCard(
                title = LanguageHelper.text(language, "Photos & Album", "படங்கள் & ஆல்பம்"),
                subtitle = LanguageHelper.text(language, "Prescriptions, Bills, Photos", "மருத்துவ சீட்டுகள், ரசீதுகள்"),
                countText = "${photos.size} " + LanguageHelper.text(language, "Files", "கோப்புகள்"),
                icon = Icons.Default.AccountBox,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                onClick = onNavigateToPhotos
            )
        }

        item {
            MainCategoryCard(
                title = LanguageHelper.text(language, "Documents (PDF & Excel)", "ஆவணங்கள் (PDF & Excel)"),
                subtitle = LanguageHelper.text(language, "Aadhaar, PAN, Bank, Sheets", "ஆதார், பான், வங்கி, எக்செல்"),
                countText = "${docs.size} " + LanguageHelper.text(language, "Files", "கோப்புகள்"),
                icon = Icons.Default.Info,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF047857))),
                onClick = onNavigateToDocs
            )
        }

        item {
            MainCategoryCard(
                title = LanguageHelper.text(language, "Smart Notes", "ஸ்மார்ட் குறிப்புகள்"),
                subtitle = LanguageHelper.text(language, "Grocery list, Daily notes", "மளிகை பட்டியல், அன்றாட குறிப்புகள்"),
                countText = "${notes.size} " + LanguageHelper.text(language, "Notes", "குறிப்புகள்"),
                icon = Icons.Default.Edit,
                gradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))),
                onClick = onNavigateToNotes
            )
        }

        item {
            MainCategoryCard(
                title = LanguageHelper.text(language, "Reminders & Alarms", "அலாரம் & நினைவூட்டல்"),
                subtitle = LanguageHelper.text(language, "Medicine time, Bill alerts", "மாத்திரை நேரம், கட்டண அலாரம்"),
                countText = "${reminders.size} " + LanguageHelper.text(language, "Alarms", "அலாரங்கள்"),
                icon = Icons.Default.Notifications,
                gradient = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFB45309))),
                onClick = onNavigateToReminders
            )
        }
    }
}
