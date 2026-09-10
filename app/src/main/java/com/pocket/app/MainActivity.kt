package com.pocket.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pocket.app.data.model.ItemType
import com.pocket.app.data.preferences.AppLanguage
import com.pocket.app.ui.home.HomeScreen
import com.pocket.app.ui.notes.NotesScreen
import com.pocket.app.ui.reminders.RemindersScreen
import com.pocket.app.ui.theme.PocketTheme
import com.pocket.app.ui.vault.VaultScreen
import com.pocket.app.ui.viewmodel.PocketViewModel
import com.pocket.app.utils.LanguageHelper

sealed class Screen(val route: String, val english: String, val tamil: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", "முகப்பு", Icons.Default.Home)
    object Photos : Screen("photos", "Photos", "படங்கள்", Icons.Default.AccountBox)
    object Docs : Screen("docs", "Docs", "கோப்புகள்", Icons.Default.Info)
    object Notes : Screen("notes", "Notes", "குறிப்புகள்", Icons.Default.Edit)
    object Reminders : Screen("reminders", "Alarms", "அலாரம்", Icons.Default.Notifications)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PocketViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val language by viewModel.language.collectAsState()

            PocketTheme(themeMode = themeMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavScreens = listOf(
                    Screen.Home,
                    Screen.Photos,
                    Screen.Docs,
                    Screen.Notes,
                    Screen.Reminders
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            bottomNavScreens.forEach { screen ->
                                val selected = currentRoute == screen.route
                                val labelText = LanguageHelper.text(language, screen.english, screen.tamil)

                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            screen.icon,
                                            contentDescription = screen.english,
                                            tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                                        )
                                    },
                                    label = {
                                        Text(
                                            labelText,
                                            fontSize = if (language == AppLanguage.BOTH) 9.sp else 11.sp,
                                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToPhotos = { navController.navigate(Screen.Photos.route) },
                                onNavigateToDocs = { navController.navigate(Screen.Docs.route) },
                                onNavigateToNotes = { navController.navigate(Screen.Notes.route) },
                                onNavigateToReminders = { navController.navigate(Screen.Reminders.route) },
                                onOpenItem = { item ->
                                    if (item.itemType == ItemType.PHOTO) navController.navigate(Screen.Photos.route)
                                    else if (item.itemType == ItemType.DOCUMENT) navController.navigate(Screen.Docs.route)
                                    else if (item.itemType == ItemType.NOTE) navController.navigate(Screen.Notes.route)
                                    else navController.navigate(Screen.Reminders.route)
                                }
                            )
                        }

                        composable(Screen.Photos.route) {
                            VaultScreen(
                                title = LanguageHelper.text(language, "Photos & Album", "படங்கள் & ஆல்பம்"),
                                targetType = ItemType.PHOTO,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Docs.route) {
                            VaultScreen(
                                title = LanguageHelper.text(language, "Documents (PDF & Excel)", "ஆவணங்கள் (PDF & Excel)"),
                                targetType = ItemType.DOCUMENT,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Notes.route) {
                            NotesScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Reminders.route) {
                            RemindersScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
