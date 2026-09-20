package com.dogusipeksac.notification_programming.di

import android.content.Context
import androidx.room.Room
import com.dogusipeksac.notification_programming.data.local.AppDatabase
import com.dogusipeksac.notification_programming.data.local.NotificationRuleDao
import com.dogusipeksac.notification_programming.data.local.PendingNotificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bildirim_zamanlayici.db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    fun provideNotificationRuleDao(db: AppDatabase): NotificationRuleDao = db.notificationRuleDao()

    @Provides
    fun providePendingNotificationDao(db: AppDatabase): PendingNotificationDao =
        db.pendingNotificationDao()
}
