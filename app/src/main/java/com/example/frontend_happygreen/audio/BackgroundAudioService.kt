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
        setupMediaPlayer()
    }

    private fun setupMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.background_music)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(0.5f, 0.5f) // Default volume at 50%
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startMusic()
        return START_STICKY // Restart if killed
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun startMusic() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                    player.prepareAsync() // Prepare for future playback
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resumeMusic() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setVolume(volume: Float) {
        try {
            mediaPlayer?.setVolume(volume, volume)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}