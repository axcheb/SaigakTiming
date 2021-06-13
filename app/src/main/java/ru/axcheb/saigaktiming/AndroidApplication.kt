package ru.axcheb.saigaktiming

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.axcheb.saigaktiming.data.di.dataModule
import ru.axcheb.saigaktiming.ui.uiModule
import timber.log.Timber

class AndroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initKoin() {
        startKoin {
            androidContext(this@AndroidApplication)
            modules(
                listOf(
                    dataModule,
                    uiModule
                )
            )
        }
    }

}