package com.elsewhere.eyris.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Dashboard : Screen("dashboard")
    object Search : Screen("search")
    object LeadsContacted : Screen("leads_contacted")
    object BusinessProfile : Screen("business_profile/{leadId}") {
        fun createRoute(leadId: String) = "business_profile/$leadId"
    }
    object Export : Screen("export")
    object Settings : Screen("settings")
}
