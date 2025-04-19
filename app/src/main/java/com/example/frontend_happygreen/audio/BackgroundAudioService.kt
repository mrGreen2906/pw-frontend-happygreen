package com.example.frontend_happygreen.audio

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.frontend_happygreen.R

class BackgroundAudioService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private val binder = AudioBinder()

    inner class AudioBinder : Binder() {
        fun getService(): BackgroundAudioService = this@BackgroundAudioService
    }

    override fun onCreate() {
        super.onCreate()
        // Create MediaPlayer and set the audio resource
        mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
        mediaPlayer?.apply {
            isLooping = true  // Loop the audio continuously
            setVolume(0.5f, 0.5f)  // Default volume
        }

        // Start playing automatically
        playAudio()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always start playing
        playAudio()

        // If service is killed, restart it
        return START_STICKY
    }

    private fun playAudio() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // Set volume method that can be called from the activity
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}
