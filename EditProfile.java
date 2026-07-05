package com.example.hershield;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfile extends AppCompatActivity {

    EditText edtName, edtEmail, edtMobile, edtPassword;
    Button btnUpdate;
    ProfileDBHelper dbHelper;
    SharedPreferences sharedPreferences;
    String activeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtMobile = findViewById(R.id.edtMobile);
        edtPassword = findViewById(R.id.edtPassword);
        btnUpdate = findViewById(R.id.buttonUpdateProfile);

        sharedPreferences = getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE);
        activeEmail = sharedPreferences.getString("username", "");

        dbHelper = new ProfileDBHelper(this);

        loadProfile();

        btnUpdate.setOnClickListener(v -> {
            String newName = edtName.getText().toString().trim();
            String newEmail = edtEmail.getText().toString().trim();
            String newMobile = edtMobile.getText().toString().trim();
            String newPassword = edtPassword.getText().toString();

            if (newName.isEmpty() || newEmail.isEmpty() || newMobile.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateProfileByEmail(activeEmail, newName, newEmail, newMobile, newPassword);

            if (updated) {
                // If updated successfully, save the new email to SharedPreferences!
                sharedPreferences.edit().putString("username", newEmail).apply();
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfile() {
        Cursor cursor = dbHelper.getProfileByEmail(activeEmail);
        if (cursor != null && cursor.moveToFirst()) {
            edtName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProfileDBHelper.COLUMN_NAME)));
            edtEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProfileDBHelper.COLUMN_EMAIL)));
            edtMobile.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProfileDBHelper.COLUMN_MOBILE)));
            edtPassword.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProfileDBHelper.COLUMN_PASSWORD)));
            cursor.close();
        }
    }
}
