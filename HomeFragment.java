package com.example.hershield;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    ImageButton sosButton;
    LinearLayout liveLocationButton, fakeCallButton, nearbyPoliceButton,
            addContactsButton, sendSMSButton, helpSupportButton,
            shakeAlertButton, sosCameraButton, nearbyHospitalButton,
            whatsappSOSButton;
    MediaPlayer mediaPlayer;
    private FusedLocationProviderClient fusedLocationClient;

    // Shake detection
    private ShakeDetector shakeDetector;
    private BuzzerHelper buzzerHelper;
    private TorchFlickerHelper torchFlickerHelper;
    private boolean shakeAlertEnabled = false;
    private boolean shakeAlertActive = false; // buzzer/torch currently running

    // SOS Camera
    private SOSCameraHelper sosCameraHelper;
    private SOSAlertManager sosAlertManager;

    // WhatsApp Broadcast
    private WhatsAppHelper whatsAppHelper;

    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize buttons
        sosButton = view.findViewById(R.id.sosButton);
        liveLocationButton = view.findViewById(R.id.liveLocationButton);
        fakeCallButton = view.findViewById(R.id.fakeCallButton);
        nearbyPoliceButton = view.findViewById(R.id.nearbyPoliceButton);
        addContactsButton = view.findViewById(R.id.addContactsButton);
        sendSMSButton = view.findViewById(R.id.sendSMSButton);
        helpSupportButton = view.findViewById(R.id.helpSupportButton);
        shakeAlertButton = view.findViewById(R.id.shakeAlertButton);
        sosCameraButton = view.findViewById(R.id.sosCameraButton);
        nearbyHospitalButton = view.findViewById(R.id.nearbyHospitalButton);
        whatsappSOSButton = view.findViewById(R.id.whatsappSOSButton);

        // Initialize helpers
        buzzerHelper = new BuzzerHelper(getActivity());
        torchFlickerHelper = new TorchFlickerHelper(getActivity());
        shakeDetector = new ShakeDetector(getActivity());
        sosAlertManager = new SOSAlertManager(getActivity());
        whatsAppHelper = new WhatsAppHelper(getActivity());

        // Load shake alert preference
        SharedPreferences prefs = getActivity().getSharedPreferences("SheShieldPrefs",
                Context.MODE_PRIVATE);
        shakeAlertEnabled = prefs.getBoolean("shakeAlertEnabled", false);
        updateShakeAlertUI(view);

        // SOS Button - plays siren
        sosButton.setOnClickListener(v -> {
            AudioManager audioManager = (AudioManager)
                    getActivity().getSystemService(Context.AUDIO_SERVICE);

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                Toast.makeText(getActivity(), "Siren Stopped", Toast.LENGTH_SHORT).show();
            } else {
                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume,
                        AudioManager.FLAG_SHOW_UI);

                mediaPlayer = MediaPlayer.create(getActivity(), R.raw.siren);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setVolume(1.0f, 1.0f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Live Location - send SMS with GPS location
        liveLocationButton.setOnClickListener(v -> {
            sendLocationAlert();
        });

        // Fake Call
        fakeCallButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FakeCallActivity.class);
            startActivity(intent);
        });

        // Nearby Police Station
        nearbyPoliceButton.setOnClickListener(v -> {
            NearbyPoliceStationFragment fragment = new NearbyPoliceStationFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Add Contacts
        addContactsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddContactActivity.class);
            startActivity(intent);
        });

        // Send SMS
        sendSMSButton.setOnClickListener(v -> {
            ContactDBHelper dbHelper = new ContactDBHelper(getContext());
            Cursor cursor = dbHelper.getAllContacts();

            List<String> emergencyContacts = new ArrayList<>();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String phone = cursor.getString(
                            cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_CONTACT));
                    emergencyContacts.add(phone);
                } while (cursor.moveToNext());
                cursor.close();
            }

            String recipients = TextUtils.join(";", emergencyContacts);
            String message = "\uD83D\uDEA8 Emergency! \uD83C\uDD98 I need help \uD83D\uDE4F"
                    + " \n \uD83D\uDCCDI am at ";

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:" + recipients));
            smsIntent.putExtra("address", recipients);
            smsIntent.putExtra("sms_body", message);

            try {
                startActivity(smsIntent);
                Toast.makeText(getActivity(), "Type your location and send!",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "No SMS app found!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Help & Support
        helpSupportButton.setOnClickListener(v -> {
            HelpFragment helpFragment = new HelpFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, helpFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // ===== NEW: Shake Alert Toggle =====
        shakeAlertButton.setOnClickListener(v -> {
            shakeAlertEnabled = !shakeAlertEnabled;

            // Save preference
            prefs.edit().putBoolean("shakeAlertEnabled", shakeAlertEnabled).apply();

            if (shakeAlertEnabled) {
                startShakeDetection();
                Toast.makeText(getActivity(),
                        "🔔 Shake Alert ON! Shake phone to trigger buzzer & torch",
                        Toast.LENGTH_LONG).show();
            } else {
                stopShakeDetection();
                // Stop buzzer/torch if currently running
                if (shakeAlertActive) {
                    buzzerHelper.stopBuzzer();
                    torchFlickerHelper.stopFlicker();
                    shakeAlertActive = false;
                }
                Toast.makeText(getActivity(), "Shake Alert OFF",
                        Toast.LENGTH_SHORT).show();
            }

            updateShakeAlertUI(view);
        });

        // ===== NEW: SOS Camera =====
        sosCameraButton.setOnClickListener(v -> {
            triggerSOSCamera();
        });

        // ===== NEW: Nearby Hospitals =====
        nearbyHospitalButton.setOnClickListener(v -> {
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=hospitals near me");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Google Maps not installed!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // ===== NEW: WhatsApp SOS Broadcast =====
        whatsappSOSButton.setOnClickListener(v -> {
            triggerWhatsAppSOS();
        });

        // Setup shake detection
        setupShakeDetection();

        return view;
    }

    // ===== WhatsApp SOS Broadcast =====

    private void triggerWhatsAppSOS() {
        if (!whatsAppHelper.isWhatsAppInstalled()) {
            Toast.makeText(getActivity(), "WhatsApp is not installed!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getActivity(), "📱 Sending SOS via SMS + WhatsApp...",
                Toast.LENGTH_SHORT).show();

        // Get location first, then send dual alert
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        whatsAppHelper.sendDualAlert(location);
                    } else {
                        // Send without location
                        whatsAppHelper.sendDualAlert(null);
                        Toast.makeText(getActivity(),
                                "Location unavailable. SOS sent without location.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    whatsAppHelper.sendDualAlert(null);
                });
    }

    // ===== Shake Detection Methods =====

    private void setupShakeDetection() {
        shakeDetector.setOnShakeListener(() -> {
            if (!shakeAlertEnabled) return;

            if (shakeAlertActive) {
                // Stop buzzer and torch
                buzzerHelper.stopBuzzer();
                torchFlickerHelper.stopFlicker();
                shakeAlertActive = false;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(), "🔕 Buzzer & Torch Stopped",
                                Toast.LENGTH_SHORT).show()
                );
            } else {
                // Start buzzer and torch
                buzzerHelper.startBuzzer();
                torchFlickerHelper.startFlicker();
                shakeAlertActive = true;
                getActivity().runOnUiThread(() ->
                        Toast.makeText(getActivity(),
                                "🚨 BUZZER & TORCH ACTIVATED! Shake again to stop.",
                                Toast.LENGTH_LONG).show()
                );
            }
        });

        if (shakeAlertEnabled) {
            startShakeDetection();
        }
    }

    private void startShakeDetection() {
        shakeDetector.start();
    }

    private void stopShakeDetection() {
        shakeDetector.stop();
    }

    private void updateShakeAlertUI(View view) {
        TextView shakeText = view.findViewById(R.id.shakeAlertText);
        if (shakeText != null) {
            shakeText.setText(shakeAlertEnabled ? "Shake\nAlert ON" : "Shake\nAlert OFF");
        }
    }

    // ===== SOS Camera Methods =====

    private void triggerSOSCamera() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
            Toast.makeText(getActivity(), "Please grant Camera permission and try again",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(getActivity(),
                "📸 SOS Camera activated! Capturing evidence...",
                Toast.LENGTH_LONG).show();

        // Play siren
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            AudioManager audioManager = (AudioManager)
                    getActivity().getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);

            mediaPlayer = MediaPlayer.create(getActivity(), R.raw.siren);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        // Start camera capture
        sosCameraHelper = new SOSCameraHelper(getActivity(), this);
        sosCameraHelper.startSOSCapture(new SOSCameraHelper.SOSCaptureCallback() {
            @Override
            public void onAllImagesCaptured(List<Uri> imageUris) {
                // Get location and send MMS
                getLocationAndSendSOS(imageUris);
            }

            @Override
            public void onCaptureError(String error) {
                Toast.makeText(getActivity(),
                        "Camera error: " + error + ". Sending SMS only.",
                        Toast.LENGTH_SHORT).show();
                // Still send SMS without images
                sendLocationAlert();
            }
        });
    }

    private void getLocationAndSendSOS(List<Uri> imageUris) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Send without location
            sosAlertManager.sendSOSWithImages(null, imageUris);
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(getActivity(), location -> {
                    sosAlertManager.sendSOSWithImages(location, imageUris);
                })
                .addOnFailureListener(e -> {
                    sosAlertManager.sendSOSWithImages(null, imageUris);
                });
    }

    // ===== Location SMS Methods =====

    private void sendLocationAlert() {
        LocationManager locationManager = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Toast.makeText(getActivity(), "Connecting to GPS...", Toast.LENGTH_LONG).show();

        if (!gpsEnabled) {
            Toast.makeText(getActivity(), "Please enable GPS", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            return;
        }

        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        sosAlertManager.sendLocationSMS(location);
                    } else {
                        Toast.makeText(getActivity(), "Unable to get current location",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shakeAlertEnabled) {
            startShakeDetection();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopShakeDetection();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (buzzerHelper != null) {
            buzzerHelper.stopBuzzer();
        }
        if (torchFlickerHelper != null) {
            torchFlickerHelper.stopFlicker();
        }
        stopShakeDetection();
    }
}
