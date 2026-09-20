package com.dogusipeksac.notification_programming.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val APP_DETAIL = "app/{packageName}"

    const val ARG_PACKAGE_NAME = "packageName"

    fun appDetail(packageName: String): String {
        val encoded = android.net.Uri.encode(packageName)
        return "app/$encoded"
    }
}
