package com.example.hershield;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class loginactivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private Button btnLogin;
    private TextView registerText;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SheShieldPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login status
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
        if (isLoggedIn) {
            openHomeActivity();
            finish();
            return;
        }

        setContentView(R.layout.activity_loginactivity);

        edtUsername = findViewById(R.id.editTextUsername);
        edtPassword = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        registerText = findViewById(R.id.registerText);

        btnLogin.setOnClickListener(v -> loginUser());

        registerText.setOnClickListener(v -> {
            startActivity(new Intent(loginactivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        ProfileDBHelper dbHelper = new ProfileDBHelper(this);

        if (dbHelper.checkLogin(username, password)) {
            // Save login status + username
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_LOGGED_IN, true);
            editor.putString(KEY_USERNAME, username);
            editor.apply();

            openHomeActivity();
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHomeActivity() {
        startActivity(new Intent(loginactivity.this, BaseActivity.class));
    }
}
