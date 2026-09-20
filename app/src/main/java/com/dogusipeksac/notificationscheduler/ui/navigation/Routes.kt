package com.dogusipeksac.notificationscheduler.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val PRIVACY = "privacy"
    const val KVKK = "kvkk"
    const val TERMS = "terms"
    const val ADD_APPS = "add_apps"
    const val APP_DETAIL = "app/{packageName}"

    const val ARG_PACKAGE_NAME = "packageName"

    fun appDetail(packageName: String): String {
        val encoded = android.net.Uri.encode(packageName)
        return "app/$encoded"
    }
}
