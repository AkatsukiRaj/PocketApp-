package com.pocket.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocket.app.data.preferences.AppLanguage
import com.pocket.app.data.preferences.AppThemeMode

@Composable
fun SettingsDialog(
    currentLanguage: AppLanguage,
    currentThemeMode: AppThemeMode,
    onLanguageChange: (AppLanguage) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "அமைப்புகள் (Settings)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Section 1: Language
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "மொழி (Language)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val languageOptions = listOf(
                        Triple(AppLanguage.ENGLISH, "English", "English only"),
                        Triple(AppLanguage.TAMIL, "தமிழ்", "தமிழ் மட்டும்"),
                        Triple(AppLanguage.BOTH, "Both (இருமொழியும்)", "English + தமிழ்")
                    )

                    languageOptions.forEach { (lang, title, desc) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLanguageChange(lang) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentLanguage == lang,
                                onClick = { onLanguageChange(lang) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Section 2: Theme
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "தீம் (Theme)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val themeOptions = listOf(
                        Triple(AppThemeMode.SYSTEM, "System Default", "போன் அமைப்புப்படி (Follow System)"),
                        Triple(AppThemeMode.SOFT_LIGHT, "Soft Light", "பகல் வெளிச்சம் (Eye-comfort Light)"),
                        Triple(AppThemeMode.SOFT_DARK, "Soft Dark", "இரவு நேரம் (Soothing Dark)")
                    )

                    themeOptions.forEach { (mode, title, desc) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange(mode) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentThemeMode == mode,
                                onClick = { onThemeChange(mode) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("முடிந்தது (Done)", fontWeight = FontWeight.Bold)
            }
        }
    )
}
