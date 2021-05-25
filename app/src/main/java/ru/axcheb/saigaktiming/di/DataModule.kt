package ru.axcheb.saigaktiming.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import org.koin.dsl.module
import ru.axcheb.saigaktiming.data.AppDatabase
import ru.axcheb.saigaktiming.data.DATABASE_NAME
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository
import ru.axcheb.saigaktiming.data.repository.SettingsRepository
import ru.axcheb.saigaktiming.workers.SeedDatabaseWorker

val dataModule = module {
    single { provideAppDatabaseInstance(get()) }
    single { MemberRepository(get<AppDatabase>().memberDao()) }
    single { EventRepository(get<AppDatabase>().eventDao()) }
    single { ResultRepository(get<AppDatabase>().resultDao()) }
    single { SettingsRepository(get<AppDatabase>().settingsDao()) }
}

private fun provideAppDatabaseInstance(context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
        .addCallback(
            object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>().build()
                    WorkManager.getInstance(context).enqueue(request)
                }
            }
        ).build()
}