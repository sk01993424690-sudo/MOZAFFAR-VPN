package com.example.ui

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object ServersRoute

@Serializable
data object SettingsRoute

@Serializable
data object PremiumRoute

@Serializable
data class LegalRoute(val docType: String)

object LegalDocTypes {
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val TERMS = "terms"
}
