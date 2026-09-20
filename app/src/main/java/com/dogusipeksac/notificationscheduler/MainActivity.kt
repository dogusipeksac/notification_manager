package com.dogusipeksac.notificationscheduler

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dogusipeksac.notificationscheduler.data.local.AppLanguage
import com.dogusipeksac.notificationscheduler.data.local.SettingsDataStore
import com.dogusipeksac.notificationscheduler.ui.locale.LocaleHelper
import com.dogusipeksac.notificationscheduler.ui.navigation.AppNavGraph
import com.dogusipeksac.notificationscheduler.ui.theme.NotificationSchedulerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var settingsDataStore: SettingsDataStore

    override fun attachBaseContext(newBase: Context) {
        val language = runCatching {
            runBlocking {
                SettingsDataStore(newBase.applicationContext).getAppLanguage()
            }
        }.getOrDefault(AppLanguage.SYSTEM)
        super.attachBaseContext(LocaleHelper.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        lifecycleScope.launch {
            // İlk değer: AppCompat locale ile senkron
            LocaleHelper.apply(settingsDataStore.getAppLanguage())
            // Sonraki değişikliklerde Activity'yi yenile (string kaynakları güncellensin)
            settingsDataStore.appLanguage
                .distinctUntilChanged()
                .drop(1)
                .collect { language ->
                    LocaleHelper.apply(language)
                    recreate()
                }
        }
        setContent {
            NotificationSchedulerTheme(darkTheme = true) {
                AppNavGraph()
            }
        }
    }
}
