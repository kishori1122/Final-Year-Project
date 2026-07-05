package com.example.hershield;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends emergency WhatsApp messages to all emergency contacts.
 * Uses WhatsApp deep link API to send to each contact individually.
 */
public class WhatsAppHelper {

    private static final String TAG = "WhatsAppHelper";
    private static final String WHATSAPP_PACKAGE = "com.whatsapp";
    private static final String WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b";
    private final Context context;

    public WhatsAppHelper(Context context) {
        this.context = context;
    }

    /**
     * Check if WhatsApp is installed on the device.
     */
    public boolean isWhatsAppInstalled() {
        PackageManager pm = context.getPackageManager();

        // Method 1: Try getLaunchIntentForPackage (works better on Android 11+)
        Intent launchIntent = pm.getLaunchIntentForPackage(WHATSAPP_PACKAGE);
        if (launchIntent != null) return true;

        launchIntent = pm.getLaunchIntentForPackage(WHATSAPP_BUSINESS_PACKAGE);
        if (launchIntent != null) return true;

        // Method 2: Try resolving a WhatsApp-specific intent
        try {
            Intent testIntent = new Intent(Intent.ACTION_SEND);
            testIntent.setType("text/plain");
            testIntent.setPackage(WHATSAPP_PACKAGE);
            if (testIntent.resolveActivity(pm) != null) return true;
        } catch (Exception ignored) {}

        // Method 3: Traditional getPackageInfo
        try {
            pm.getPackageInfo(WHATSAPP_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}

        try {
            pm.getPackageInfo(WHATSAPP_BUSINESS_PACKAGE, 0);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}

        return false;
    }

    /**
     * Get the WhatsApp package name that's installed.
     */
    private String getWhatsAppPackage() {
        PackageManager pm = context.getPackageManager();
        if (pm.getLaunchIntentForPackage(WHATSAPP_PACKAGE) != null) {
            return WHATSAPP_PACKAGE;
        }
        if (pm.getLaunchIntentForPackage(WHATSAPP_BUSINESS_PACKAGE) != null) {
            return WHATSAPP_BUSINESS_PACKAGE;
        }
        return WHATSAPP_PACKAGE; // default
    }

    /**
     * Send SOS message to each emergency contact individually via WhatsApp.
     * Opens WhatsApp chat with message pre-filled for each contact.
     */
    public void sendWhatsAppToAllContacts(Location location) {
        List<String> contacts = getEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts added! Please add contacts first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!isWhatsAppInstalled()) {
            Toast.makeText(context, "WhatsApp is not installed on this device!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String message = buildSOSMessage(location);
        String whatsAppPkg = getWhatsAppPackage();

        // Send to first contact immediately
        sendToContact(contacts.get(0), message, whatsAppPkg);

        Toast.makeText(context,
                "📱 WhatsApp SOS: Sending to " + contacts.size() + " contacts. "
                        + "Tap Send in WhatsApp for each contact.",
                Toast.LENGTH_LONG).show();

        // If more contacts, send with delays
        if (contacts.size() > 1) {
            Handler handler = new Handler(Looper.getMainLooper());
            for (int i = 1; i < contacts.size(); i++) {
                final String phone = contacts.get(i);
                handler.postDelayed(() -> {
                    sendToContact(phone, message, whatsAppPkg);
                }, i * 2000L); // 2 sec delay between each
            }
        }
    }

    /**
     * Send WhatsApp message to a single contact using the wa.me deep link.
     * This directly opens the chat with that contact with the message pre-filled.
     */
    private void sendToContact(String phone, String message, String whatsAppPkg) {
        try {
            String formattedPhone = formatPhoneForWhatsApp(phone);
            String encodedMessage = URLEncoder.encode(message, "UTF-8");

            // Use wa.me link - directly opens chat with the contact
            String url = "https://wa.me/" + formattedPhone + "?text=" + encodedMessage;

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setPackage(whatsAppPkg);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);

            Log.d(TAG, "WhatsApp chat opened for: " + formattedPhone);

        } catch (Exception e) {
            Log.e(TAG, "WhatsApp send failed for: " + phone, e);
        }
    }

    /**
     * Send location to all contacts via both SMS and WhatsApp.
     * SMS is sent automatically, WhatsApp opens with contacts one by one.
     */
    public void sendDualAlert(Location location) {
        List<String> contacts = getEmergencyContacts();

        if (contacts.isEmpty()) {
            Toast.makeText(context, "No emergency contacts! Add contacts first.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // 1. Send SMS to all contacts automatically
        SOSAlertManager smsManager = new SOSAlertManager(context);
        smsManager.sendLocationSMS(location);

        // 2. Send WhatsApp to all contacts
        if (isWhatsAppInstalled()) {
            sendWhatsAppToAllContacts(location);
        } else {
            Toast.makeText(context,
                    "✅ SMS sent! WhatsApp not installed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Create a WhatsApp broadcast by opening WhatsApp with a formatted
     * phone number list for the user to create a broadcast list.
     * 
     * Since WhatsApp doesn't allow programmatic broadcast creation,
     * Opens WhatsApp's "New Broadcast" screen directly.
     * 
     * WhatsApp does NOT allow pre-selecting contacts programmatically (security
     * restriction by Meta), but we can open the broadcast creation page directly
     * so the user just needs to search and tap their emergency contacts.
     * 
     * A toast shows the contact names/numbers to help the user select them.
     */
    public void createWhatsAppBroadcast(List<Contact> contactList) {
        if (contactList == null || contactList.isEmpty()) {
            Toast.makeText(context, "No emergency contacts to broadcast to!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (!isWhatsAppInstalled()) {
            Toast.makeText(context, "WhatsApp is not installed!",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String whatsAppPkg = getWhatsAppPackage();

        // Build contact names for the toast
        StringBuilder contactInfo = new StringBuilder();
        for (int i = 0; i < contactList.size(); i++) {
            Contact c = contactList.get(i);
            contactInfo.append("• ").append(c.getFname()).append(" ").append(c.getLname())
                    .append(" (").append(c.getPhone()).append(")");
            if (i < contactList.size() - 1) contactInfo.append("\n");
        }

        // Try to open WhatsApp's New Broadcast screen directly
        boolean opened = false;

        // Method 1: Try WhatsApp's internal NewBroadcastActivity
        try {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setClassName(whatsAppPkg,
                    "com.whatsapp.NewBroadcastActivity");
            broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(broadcastIntent);
            opened = true;
        } catch (Exception e) {
            Log.d(TAG, "NewBroadcastActivity not found, trying alternative...");
        }

        // Method 2: Try with component name variation
        if (!opened) {
            try {
                Intent broadcastIntent = new Intent();
                broadcastIntent.setClassName(whatsAppPkg,
                        "com.whatsapp.broadcast.NewBroadcastActivity");
                broadcastIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(broadcastIntent);
                opened = true;
            } catch (Exception e) {
                Log.d(TAG, "broadcast.NewBroadcastActivity not found, using share...");
            }
        }

        // Method 3: Fallback — open WhatsApp share screen
        if (!opened) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.setPackage(whatsAppPkg);
                shareIntent.putExtra(Intent.EXTRA_TEXT,
                        "\uD83D\uDEA8 *EMERGENCY SOS ALERT!* \uD83C\uDD98\n\n"
                        + "I need immediate help! This is an SOS alert from *SheShield* app.\n\n"
                        + "\uD83D\uDE4F *Please help me!*\n\n"
                        + "⚠️ _This is an automated emergency alert from SheShield._\n"
                        + "_Your Safety. Our Priority._");
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(shareIntent);
                opened = true;
            } catch (Exception e) {
                Log.e(TAG, "All WhatsApp methods failed", e);
            }
        }

        if (opened) {
            // Show which contacts to select
            Toast.makeText(context,
                    "📱 Select these contacts in WhatsApp:\n" + contactInfo,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Failed to open WhatsApp!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Build the SOS emergency message with location.
     */
    private String buildSOSMessage(Location location) {
        StringBuilder message = new StringBuilder();
        message.append("\uD83D\uDEA8 *EMERGENCY SOS ALERT!* \uD83C\uDD98\n\n");
        message.append("I need immediate help! This is an SOS alert from *SheShield* app.\n\n");
        message.append("\uD83D\uDE4F *Please help me!*\n");

        if (location != null) {
            String locationUrl = "https://www.google.com/maps/search/?api=1&query="
                    + location.getLatitude() + "," + location.getLongitude();
            message.append("\n\uD83D\uDCCD *My Live Location:*\n");
            message.append(locationUrl).append("\n");
            message.append("\nLatitude: ").append(location.getLatitude());
            message.append("\nLongitude: ").append(location.getLongitude()).append("\n");
        }

        message.append("\n⚠️ _This is an automated emergency alert from SheShield._");
        message.append("\n_Your Safety. Our Priority._");

        return message.toString();
    }

    /**
     * Format phone number for WhatsApp API (needs country code, no +, no spaces).
     * If number doesn't start with country code, assumes India (+91).
     */
    private String formatPhoneForWhatsApp(String phone) {
        // Remove all non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");

        // If starts with 0, remove it and add 91 (India)
        if (cleaned.startsWith("0")) {
            cleaned = "91" + cleaned.substring(1);
        }

        // If 10 digits (Indian number without country code), add 91
        if (cleaned.length() == 10) {
            cleaned = "91" + cleaned;
        }

        return cleaned;
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
