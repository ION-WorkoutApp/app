package com.ion606.workoutapp.managers

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.ion606.workoutapp.R;


class CustomAudioManager {
    companion object {
        private var mediaPlayer: MediaPlayer? = null;
        private var focusRequest: AudioFocusRequest? = null;

        @RequiresApi(Build.VERSION_CODES.O)
        fun playNotificationSound(context: Context, notifCB: (() -> Unit)? = null) {
            val sharedPrefs = context.getSharedPreferences("localprefs", Context.MODE_PRIVATE);
            val soundResId = sharedPrefs.getInt("notifsound", R.raw.chime);

            if (soundResId == 0) {
                notifCB?.invoke();
                return;
            }

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager;

            val playbackAttributes =
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

            val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        Log.d("AudioManager", "audio focus gained");
                        if (mediaPlayer == null) {
                            mediaPlayer =
                                MediaPlayer.create(context.applicationContext, soundResId).apply {
                                    setOnCompletionListener {
                                        Log.d("AudioManager", "sound completed");
                                        releaseMediaPlayer();
                                        abandonFocus(audioManager);
                                    };
                                    start();
                                };
                        } else {
                            mediaPlayer?.start();
                        }
                    }

                    AudioManager.AUDIOFOCUS_LOSS -> {
                        Log.d("AudioManager", "audio focus lost");
                        releaseMediaPlayer();
                        abandonFocus(audioManager);
                    }

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        Log.d("AudioManager", "audio focus lost transient");
                        mediaPlayer?.pause();
                    }

                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        Log.d("AudioManager", "audio focus lost transient, can duck");
                        mediaPlayer?.setVolume(0.5f, 0.5f);
                    }
                }
            };

            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                .setAudioAttributes(playbackAttributes).setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener).build();

            val result = audioManager.requestAudioFocus(focusRequest!!);
            when (result) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    Log.d("AudioManager", "transient audio focus granted immediately");

                    // manually trigger playback since focus was granted immediately
                    audioFocusChangeListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
                }

                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> Log.e(
                    "AudioManager", "audio focus request failed"
                );
                else -> Log.d("AudioManager", "audio focus request delayed");
            }
        }

        private fun releaseMediaPlayer() {
            mediaPlayer?.release();
            mediaPlayer = null;
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun abandonFocus(audioManager: AudioManager) {
            focusRequest?.let {
                audioManager.abandonAudioFocusRequest(it);
                focusRequest = null;
            }
        }
    }
}
