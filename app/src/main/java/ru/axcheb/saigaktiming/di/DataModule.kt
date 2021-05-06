package ru.axcheb.saigaktiming.data.di

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module
import ru.axcheb.saigaktiming.data.AppDatabase
import ru.axcheb.saigaktiming.data.DATABASE_NAME
import ru.axcheb.saigaktiming.data.repository.EventRepository
import ru.axcheb.saigaktiming.data.repository.MemberRepository
import ru.axcheb.saigaktiming.data.repository.ResultRepository

val dataModule = module {
    single { provideAppDatabaseInstance(get()) }
    single { MemberRepository(get<AppDatabase>().memberDao()) }
    single { EventRepository(get<AppDatabase>().eventDao()) }
    single { ResultRepository(get<AppDatabase>().resultDao()) }

}

private fun provideAppDatabaseInstance(context: Context): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
}