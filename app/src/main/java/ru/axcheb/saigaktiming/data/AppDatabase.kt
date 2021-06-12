package ru.axcheb.saigaktiming.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.axcheb.saigaktiming.data.dao.EventDao
import ru.axcheb.saigaktiming.data.dao.MemberDao
import ru.axcheb.saigaktiming.data.dao.ResultDao
import ru.axcheb.saigaktiming.data.dao.SettingsDao
import ru.axcheb.saigaktiming.data.model.db.*

@Database(
    entities = [
        Member::class,
        Event::class,
        EventMemberCrossRef::class,
        Start::class,
        Finish::class,
        Settings::class
     ],
    version = 1,
    exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun memberDao(): MemberDao
    abstract fun eventDao(): EventDao
    abstract fun resultDao(): ResultDao
    abstract fun settingsDao(): SettingsDao

}