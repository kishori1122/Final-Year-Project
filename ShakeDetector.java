package com.example.hershield;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Detects phone shaking using accelerometer.
 * Triggers callback when user shakes phone vigorously (3 strong shakes in ~1 second).
 */
public class ShakeDetector implements SensorEventListener {

    private static final String TAG = "ShakeDetector";
    private static final float SHAKE_THRESHOLD = 12.0f; // m/s²
    private static final int SHAKE_COUNT_THRESHOLD = 3;
    private static final long SHAKE_TIME_WINDOW = 1500; // ms
    private static final long MIN_TIME_BETWEEN_SHAKES = 300; // ms

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private OnShakeListener listener;

    private long lastShakeTime = 0;
    private int shakeCount = 0;
    private long firstShakeTime = 0;

    public interface OnShakeListener {
        void onShakeDetected();
    }

    public ShakeDetector(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void setOnShakeListener(OnShakeListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
            Log.d(TAG, "Shake detector started");
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "Shake detector stopped");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Calculate acceleration magnitude (minus gravity)
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z)
                - SensorManager.GRAVITY_EARTH;

        long currentTime = System.currentTimeMillis();

        if (acceleration > SHAKE_THRESHOLD) {
            if (currentTime - lastShakeTime > MIN_TIME_BETWEEN_SHAKES) {
                if (shakeCount == 0) {
                    firstShakeTime = currentTime;
                }

                shakeCount++;
                lastShakeTime = currentTime;

                Log.d(TAG, "Shake detected! Count: " + shakeCount);

                    if (currentTime - firstShakeTime <= SHAKE_TIME_WINDOW) {
                        // Shake threshold met!
                        shakeCount = 0;
                        if (listener != null) {
                            listener.onShakeDetected();
                        }
                    } else {
                        // Too slow, reset
                        shakeCount = 1;
                        firstShakeTime = currentTime;
                    }
                }
            }
        }

        // Reset if too much time passed
        if (currentTime - firstShakeTime > SHAKE_TIME_WINDOW && shakeCount > 0) {
            shakeCount = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed
    }
}
