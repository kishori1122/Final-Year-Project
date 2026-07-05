package com.example.hershield;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Rapidly flickers the phone flashlight (strobe effect) for emergency alerting.
 * Pattern: 100ms on / 100ms off (creates a visible strobe).
 */
public class TorchFlickerHelper {

    private static final String TAG = "TorchFlicker";
    private static final long FLICKER_ON_MS = 100;
    private static final long FLICKER_OFF_MS = 100;

    private final CameraManager cameraManager;
    private String cameraId;
    private HandlerThread handlerThread;
    private Handler handler;
    private boolean isFlickering = false;
    private boolean torchOn = false;

    public TorchFlickerHelper(Context context) {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            // Get the back camera ID (which has the flash)
            String[] cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length > 0) {
                cameraId = cameraIds[0]; // Usually back camera
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Cannot access camera", e);
        }
    }

    /**
     * Start the strobe/flicker effect.
     */
    public void startFlicker() {
        if (isFlickering || cameraId == null) return;

        isFlickering = true;
        handlerThread = new HandlerThread("TorchFlickerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        handler.post(flickerRunnable);
        Log.d(TAG, "Torch flicker started");
    }

    /**
     * Stop the strobe/flicker effect and turn off torch.
     */
    public void stopFlicker() {
        isFlickering = false;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }

        // Ensure torch is off
        setTorch(false);

        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        handler = null;
        Log.d(TAG, "Torch flicker stopped");
    }

    public boolean isFlickering() {
        return isFlickering;
    }

    private final Runnable flickerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isFlickering) return;

            torchOn = !torchOn;
            setTorch(torchOn);

            long delay = torchOn ? FLICKER_ON_MS : FLICKER_OFF_MS;
            if (handler != null) {
                handler.postDelayed(this, delay);
            }
        }
    };

    private void setTorch(boolean on) {
        try {
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, on);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Torch control failed", e);
        }
    }
}
