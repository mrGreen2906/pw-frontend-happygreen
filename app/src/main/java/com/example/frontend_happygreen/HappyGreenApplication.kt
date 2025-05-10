package com.example.frontend_happygreen

import android.app.Application
import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.frontend_happygreen.data.UserSession

/**
 * Classe Application personalizzata per inizializzare componenti globali.
 * Include monitoraggio del ciclo di vita dell'applicazione per mantenere la sessione utente.
 */
class HappyGreenApplication : Application(), DefaultLifecycleObserver {

    companion object {
        // Riferimento statico all'istanza dell'applicazione
        private lateinit var instance: HappyGreenApplication

        fun getAppContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super<Application>.onCreate()
        instance = this

        // Registra osservatore del ciclo di vita utilizzando DefaultLifecycleObserver
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Inizializza UserSession con il contesto dell'applicazione
        UserSession.init(applicationContext)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Salva lo stato dell'applicazione quando va in background
        val prefs = getSharedPreferences("app_state", MODE_PRIVATE)
        prefs.edit().putLong("last_background_time", System.currentTimeMillis()).apply()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Quando l'app torna in foreground, controlla quanto tempo è passato
        val prefs = getSharedPreferences("app_state", MODE_PRIVATE)
        val lastBackgroundTime = prefs.getLong("last_background_time", 0)

        // Tempo massimo di inattività: 24 ore (configurabile)
        val inactivityThreshold = 24 * 60 * 60 * 1000L // 24 ore in millisecondi

        if (System.currentTimeMillis() - lastBackgroundTime > inactivityThreshold) {
            // Se è passato troppo tempo, forza il logout per sicurezza
            // Ma questo è configurabile in base alle tue esigenze di sicurezza
            // Se vuoi che la sessione persista indefinitamente, puoi commentare questo blocco
            // UserSession.clear()
        }
    }

    // Altri metodi del ciclo di vita che potresti voler monitorare
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        // Puoi aggiungere logica qui se necessario
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Puoi aggiungere logica qui se necessario
    }
}