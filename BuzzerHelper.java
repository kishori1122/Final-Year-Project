package com.example.hershield;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

/**
 * Plays a loud buzzer/alarm sound at maximum volume with vibration
 * for emergency alerting when phone is shaken.
 */
public class BuzzerHelper {

    private static final String TAG = "BuzzerHelper";
    private MediaPlayer mediaPlayer;
    private final Context context;
    private final Vibrator vibrator;
    private boolean isPlaying = false;

    public BuzzerHelper(Context context) {
        this.context = context;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Start the buzzer alarm at maximum volume with vibration.
     */
    public void startBuzzer() {
        if (isPlaying) return;

        try {
            // Set volume to maximum
            AudioManager audioManager = (AudioManager)
                    context.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume,
                    AudioManager.FLAG_SHOW_UI);

            // Try to use the siren from raw resources first
            try {
                mediaPlayer = MediaPlayer.create(context, R.raw.siren);
            } catch (Exception e) {
                // Fallback to default alarm sound
                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                }
                mediaPlayer = MediaPlayer.create(context, alarmUri);
            }

            if (mediaPlayer != null) {
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }

            // Start vibration pattern: vibrate 500ms, pause 200ms, repeat
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 500, 200, 500, 200, 1000};
                vibrator.vibrate(pattern, 0); // 0 = repeat from start
            }

            isPlaying = true;
            Log.d(TAG, "Buzzer started");

        } catch (Exception e) {
            Log.e(TAG, "Failed to start buzzer", e);
        }
    }

    /**
     * Stop the buzzer and vibration.
     */
    public void stopBuzzer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }

        isPlaying = false;
        Log.d(TAG, "Buzzer stopped");
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
