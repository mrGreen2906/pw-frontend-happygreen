package com.example.frontend_happygreen

import android.app.Application
import com.example.frontend_happygreen.data.UserSession

/**
 * Classe Application personalizzata per inizializzare componenti globali
 *
 * Ricordati di aggiungerla al manifest:
 * <application
 *
 *    ...
 * >
 */
class HappyGreenApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inizializza UserSession
        UserSession.init(this)
    }
}