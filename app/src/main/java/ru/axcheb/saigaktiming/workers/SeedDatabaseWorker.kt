package ru.axcheb.saigaktiming.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.coroutineScope
import org.koin.java.KoinJavaComponent.inject
import ru.axcheb.saigaktiming.data.AppDatabase
import ru.axcheb.saigaktiming.data.SETTINGS_DATA_FILE_NAME
import ru.axcheb.saigaktiming.data.model.db.Settings
import timber.log.Timber

class SeedDatabaseWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val appDatabase: AppDatabase by inject(AppDatabase::class.java)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            applicationContext.assets.open(SETTINGS_DATA_FILE_NAME).use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val settings: Settings =
                        Gson().fromJson(jsonReader, object : TypeToken<Settings>() {}.type)
                    appDatabase.settingsDao().insert(settings)
                    Result.success()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error seeding database")
            Result.failure()
        }
    }
}