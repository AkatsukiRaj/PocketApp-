package com.pocket.app.utils

import com.pocket.app.data.preferences.AppLanguage

object LanguageHelper {

    fun text(lang: AppLanguage, english: String, tamil: String): String {
        return when (lang) {
            AppLanguage.ENGLISH -> english
            AppLanguage.TAMIL -> tamil
            AppLanguage.BOTH -> "$english ($tamil)"
        }
    }

    fun titleSubtitle(lang: AppLanguage, english: String, tamil: String): Pair<String, String?> {
        return when (lang) {
            AppLanguage.ENGLISH -> Pair(english, null)
            AppLanguage.TAMIL -> Pair(tamil, null)
            AppLanguage.BOTH -> Pair(english, tamil)
        }
    }
}
