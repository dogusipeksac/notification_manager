package com.dogusipeksac.notificationscheduler.ui.locale

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.dogusipeksac.notificationscheduler.data.local.AppLanguage
import java.util.Locale

object LocaleHelper {

    fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(toLocaleList(language))
    }

    fun wrap(base: Context, language: AppLanguage): Context {
        val locale = when (language) {
            AppLanguage.SYSTEM -> return base
            AppLanguage.TURKISH -> Locale.forLanguageTag("tr")
            AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
        }
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLocales(android.os.LocaleList(locale))
        return base.createConfigurationContext(config)
    }

    private fun toLocaleList(language: AppLanguage): LocaleListCompat {
        return when (language) {
            AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
            AppLanguage.TURKISH -> LocaleListCompat.forLanguageTags("tr")
            AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        }
    }
}
