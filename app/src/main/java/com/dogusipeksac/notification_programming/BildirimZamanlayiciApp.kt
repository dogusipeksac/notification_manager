package com.dogusipeksac.notification_programming

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dogusipeksac.notification_programming.service.DelayedNotificationPoster
import com.dogusipeksac.notification_programming.service.RuleCache
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class BildirimZamanlayiciApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var ruleCache: RuleCache
    @Inject lateinit var delayedNotificationPoster: DelayedNotificationPoster

    override fun onCreate() {
        super.onCreate()
        // Listener bağlanmadan önce cache dolu olsun diye ilk yüklemeyi bekliyoruz.
        runBlocking { ruleCache.loadNow() }
        ruleCache.startObserving()
        delayedNotificationPoster.ensureChannel()
    }

    /**
     * HiltWorkerFactory'nin kullanılması için varsayılan WorkManager initializer
     * manifest'ten kaldırıldı; yapılandırma buradan verilir.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
