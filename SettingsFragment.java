package com.example.hershield;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.profile_settings).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfile.class);
            startActivity(intent);
        });

        view.findViewById(R.id.emergency_contacts).setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new ManageContacts());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        view.findViewById(R.id.safety_tips).setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new SafetyTipsFraGment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        view.findViewById(R.id.about_app).setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new AboutInfoFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        view.findViewById(R.id.help_app).setOnClickListener(v -> {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new HelpFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        view.findViewById(R.id.logout_settings).setOnClickListener(v -> {
            Snackbar.make(view, "You are Logged Out!", Snackbar.LENGTH_SHORT).show();
            logout();
        });
    }
    private void logout() {
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear(); // clears all keys (isLoggedIn, username)
        editor.apply();

        // Go back to login
        startActivity(new Intent(requireContext(), loginactivity.class));
        requireActivity().finish();
    }


}
