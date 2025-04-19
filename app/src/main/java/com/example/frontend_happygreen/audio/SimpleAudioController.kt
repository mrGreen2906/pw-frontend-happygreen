package com.example.frontend_happygreen.audio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

/**
 * Audio controller that can start the music and adjust volume
 */
class AudioController(private val context: Context) {
    private var audioService: BackgroundAudioService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BackgroundAudioService.AudioBinder
            audioService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            bound = false
        }
    }

    fun startBackgroundMusic() {
        // Start the service
        val intent = Intent(context, BackgroundAudioService::class.java)
        context.startService(intent)

        // Bind to the service to be able to control volume
        context.bindService(
            Intent(context, BackgroundAudioService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun setVolume(volume: Float) {
        audioService?.setVolume(volume)
    }

    fun unbind() {
        if (bound) {
            context.unbindService(serviceConnection)
            bound = false
        }
    }
}