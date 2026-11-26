package com.example.localization

import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    BENGALI("bn", "Bengali", "বাংলা");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
