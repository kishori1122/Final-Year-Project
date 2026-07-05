package com.example.hershield;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        SharedPreferences sharedPreferences = getSharedPreferences("SheShieldPrefs", MODE_PRIVATE);
        // Also check old prefs for backward compatibility
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            SharedPreferences oldPrefs = getSharedPreferences("HerShieldPrefs", MODE_PRIVATE);
            if (oldPrefs.getBoolean("isLoggedIn", false)) {
                // Migrate to new prefs
                sharedPreferences.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("username", oldPrefs.getString("username", ""))
                        .apply();
            }
        }
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(this, loginactivity.class));
            finish();
            return;
        }

        bottomNavigationView = findViewById(R.id.bottom);

        String openFragment = null;
        if (getIntent() != null) {
            openFragment = getIntent().getStringExtra("openFragment");
        }

        if (savedInstanceState == null) {
            if ("manage_contacts".equals(openFragment)) {
                loadFragment(new ManageContacts());
                bottomNavigationView.setSelectedItemId(R.id.nav_contacts);
            } else {
                loadFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (item.getItemId() == R.id.nav_safety_tips) {
                loadFragment(new SafetyTipsFraGment());
                return true;
            } else if (item.getItemId() == R.id.nav_contacts) {
                loadFragment(new ManageContacts());
                return true;
            } else if (item.getItemId() == R.id.nav_settings) {
                loadFragment(new SettingsFragment());
                return true;
            } else {
                loadFragment(new HomeFragment());
                return true;
            }
        });
    }
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}