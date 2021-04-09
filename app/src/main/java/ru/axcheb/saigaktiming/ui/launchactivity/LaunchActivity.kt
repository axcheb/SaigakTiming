package ru.axcheb.saigaktiming.ui.launchactivity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.axcheb.saigaktiming.ui.MainActivity

/**
 * Активити, с которой стартует приложение.
 * Она не имеет UI. Она просто стартует дуругую Активити. В зависимости от различных условий может
 * стартануть нужную активити при старте приложения.
 */
class LaunchActivity: AppCompatActivity()  {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.start(this)
        finish()
    }
}