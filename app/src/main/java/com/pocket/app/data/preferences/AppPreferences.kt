package com.pocket.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage {
    ENGLISH,
    TAMIL,
    BOTH
}

enum class AppThemeMode {
    SYSTEM,
    SOFT_LIGHT,
    SOFT_DARK
}

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("pocket_app_prefs", Context.MODE_PRIVATE)

    private val _language = MutableStateFlow(loadLanguage())
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private fun loadLanguage(): AppLanguage {
        val name = prefs.getString(KEY_LANGUAGE, AppLanguage.BOTH.name)
        return runCatching { AppLanguage.valueOf(name!!) }.getOrDefault(AppLanguage.BOTH)
    }

    private fun loadThemeMode(): AppThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name)
        return runCatching { AppThemeMode.valueOf(name!!) }.getOrDefault(AppThemeMode.SYSTEM)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString(KEY_LANGUAGE, language.name).apply()
        _language.value = language
    }

    fun setThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _themeMode.value = themeMode
    }

    companion object {
        private const val KEY_LANGUAGE = "key_app_language"
        private const val KEY_THEME_MODE = "key_app_theme_mode"
    }
}
