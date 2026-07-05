package com.example.hershield;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Helper class to silently capture images from front and back cameras
 * for SOS emergency alerts.
 */
public class SOSCameraHelper {

    private static final String TAG = "SOSCameraHelper";
    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;
    private final List<Uri> capturedImageUris = new ArrayList<>();
    private int totalCaptures;
    private int capturesDone = 0;
    private SOSCaptureCallback callback;

    public interface SOSCaptureCallback {
        void onAllImagesCaptured(List<Uri> imageUris);
        void onCaptureError(String error);
    }

    public SOSCameraHelper(Context context, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
    }

    /**
     * Start capturing 4-6 random images alternating between front and back cameras.
     */
    public void startSOSCapture(SOSCaptureCallback callback) {
        this.callback = callback;
        this.capturedImageUris.clear();
        this.capturesDone = 0;

        Random random = new Random();
        this.totalCaptures = 4 + random.nextInt(3); // 4 to 6 images

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(context);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                captureNextImage();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera init failed", e);
                if (callback != null) {
                    callback.onCaptureError("Camera initialization failed");
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void captureNextImage() {
        if (capturesDone >= totalCaptures) {
            // All captures done
            if (cameraProvider != null) {
                cameraProvider.unbindAll();
            }
            if (callback != null) {
                callback.onAllImagesCaptured(capturedImageUris);
            }
            return;
        }

        // Alternate between front and back camera
        boolean useFrontCamera = (capturesDone % 2 == 0);

        try {
            cameraProvider.unbindAll();

            CameraSelector cameraSelector = useFrontCamera
                    ? CameraSelector.DEFAULT_FRONT_CAMERA
                    : CameraSelector.DEFAULT_BACK_CAMERA;

            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture);

            // Small delay to let camera initialize before capture
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                takePicture(useFrontCamera ? "front" : "back");
            }, 800);

        } catch (Exception e) {
            Log.e(TAG, "Camera bind failed, skipping capture " + capturesDone, e);
            capturesDone++;
            captureNextImage();
        }
    }

    private void takePicture(String cameraFacing) {
        if (imageCapture == null) {
            capturesDone++;
            captureNextImage();
            return;
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.getDefault())
                .format(new Date());
        String fileName = "SOS_" + cameraFacing + "_" + timestamp + ".jpg";

        ImageCapture.OutputFileOptions outputOptions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/SheShield_SOS");

            outputOptions = new ImageCapture.OutputFileOptions.Builder(
                    context.getContentResolver(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
            ).build();
        } else {
            // Use file for older Android
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "SheShield_SOS");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            outputOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        }

        imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(context),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults results) {
                        Uri savedUri = results.getSavedUri();
                        if (savedUri != null) {
                            capturedImageUris.add(savedUri);
                            Log.d(TAG, "Image captured: " + savedUri);
                        }
                        capturesDone++;

                        // Random delay between captures (300-800ms)
                        int delay = 300 + new Random().nextInt(500);
                        new android.os.Handler(android.os.Looper.getMainLooper())
                                .postDelayed(() -> captureNextImage(), delay);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "Capture failed: " + exception.getMessage());
                        capturesDone++;
                        captureNextImage();
                    }
                });
    }
}
