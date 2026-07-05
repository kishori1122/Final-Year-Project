package com.example.hershield;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages sending SOS alerts via MMS (with images) and SMS (text fallback)
 * to all emergency contacts with live location.
 */
public class SOSAlertManager {

    private static final String TAG = "SOSAlertManager";
    private final Context context;

    public SOSAlertManager(Context context) {
        this.context = context;
    }

    /**
     * Send MMS with captured images and location to all emergency contacts.
     */
    public void sendSOSWithImages(Location location, List<Uri> imageUris) {
        List<String> emergencyContacts = getEmergencyContacts();

        if (emergencyContacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts added! Please add contacts first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String locationUrl = "";
        if (location != null) {
            locationUrl = "https://www.google.com/maps/search/?api=1&query="
                    + location.getLatitude() + "," + location.getLongitude();
        }

        String message = "\uD83D\uDEA8 EMERGENCY SOS ALERT! \uD83C\uDD98\n\n"
                + "I need immediate help! This is an SOS alert from SheShield app.\n\n"
                + "\uD83D\uDE4F Please help me!\n";

        if (!locationUrl.isEmpty()) {
            message += "\n\uD83D\uDCCD My Live Location:\n" + locationUrl + "\n";
        }

        message += "\n\uD83D\uDCF7 " + imageUris.size() + " photos captured and attached.\n"
                + "\n⚠️ This is an automated emergency alert.";

        // Try MMS with images first
        if (imageUris != null && !imageUris.isEmpty()) {
            sendMMS(emergencyContacts, message, imageUris);
        }

        // Also send SMS as backup (MMS may not go through on all carriers)
        sendSMSBackup(emergencyContacts, location);
    }

    /**
     * Send MMS via intent with multiple images attached.
     */
    private void sendMMS(List<String> contacts, String message, List<Uri> imageUris) {
        try {
            String recipients = TextUtils.join(";", contacts);

            Intent mmsIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            mmsIntent.setType("image/*");
            mmsIntent.putExtra("address", recipients);
            mmsIntent.putExtra("sms_body", message);
            mmsIntent.putExtra(Intent.EXTRA_TEXT, message);

            // Attach all images
            ArrayList<Uri> uriList = new ArrayList<>(imageUris);

            // Grant read permission for each URI
            for (Uri uri : uriList) {
                context.grantUriPermission("com.android.mms",
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            mmsIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            mmsIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            mmsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(Intent.createChooser(mmsIntent, "Send SOS Alert via"));

            Toast.makeText(context,
                    "📸 " + imageUris.size() + " photos captured! Sending SOS MMS...",
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Log.e(TAG, "MMS send failed", e);
            Toast.makeText(context, "MMS failed. Sending SMS backup...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Send SMS backup with location link to all emergency contacts.
     */
    private void sendSMSBackup(List<String> contacts, Location location) {
        try {
            String message = "\uD83D\uDEA8 SOS! I need help urgently! \uD83C\uDD98\n"
                    + "Sent from SheShield App.\n";

            if (location != null) {
                message += "\uD83D\uDCCD Location: https://www.google.com/maps/search/?api=1&query="
                        + location.getLatitude() + "," + location.getLongitude();
            }

            SmsManager smsManager = SmsManager.getDefault();
            for (String contact : contacts) {
                try {
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    smsManager.sendMultipartTextMessage(contact, null, parts, null, null);
                } catch (Exception e) {
                    Log.e(TAG, "SMS to " + contact + " failed", e);
                }
            }

            Log.d(TAG, "SMS backup sent to " + contacts.size() + " contacts");
        } catch (Exception e) {
            Log.e(TAG, "SMS backup failed", e);
        }
    }

    /**
     * Send SMS-only alert (no images) with location.
     */
    public void sendLocationSMS(Location location) {
        List<String> contacts = getEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts! Add contacts first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String message = "\uD83D\uDEA8 Help! Emergency! I need help.\n"
                + "Sent from SheShield App.\n\n"
                + "\uD83D\uDCCD My current location:\n"
                + "https://www.google.com/maps/search/?api=1&query="
                + location.getLatitude() + "," + location.getLongitude();

        SmsManager smsManager = SmsManager.getDefault();
        for (String contact : contacts) {
            try {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(contact, null, parts, null, null);
            } catch (Exception e) {
                Log.e(TAG, "SMS failed to " + contact, e);
            }
        }

        Toast.makeText(context, "🚨 Emergency SMS sent to " + contacts.size() + " contacts!",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Get all emergency contacts from database.
     */
    private List<String> getEmergencyContacts() {
        List<String> contacts = new ArrayList<>();
        ContactDBHelper dbHelper = new ContactDBHelper(context);
        Cursor cursor = dbHelper.getAllContacts();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String phone = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactDBHelper.COLUMN_CONTACT));
                contacts.add(phone);
            } while (cursor.moveToNext());
            cursor.close();
        }

        return contacts;
    }
}
