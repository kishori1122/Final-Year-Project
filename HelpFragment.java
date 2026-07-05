package com.example.hershield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class HelpFragment extends Fragment {

    public HelpFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        Button emergencyBtn = view.findViewById(R.id.emergencyBtn);
        emergencyBtn.setOnClickListener(v -> {
            String phoneNumber = "112";
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(dialIntent);
        });

        Button feedbackBtn = view.findViewById(R.id.feedbackBtn);
        feedbackBtn.setOnClickListener(v -> {
            FeedbackFragment feedbackFragment = new FeedbackFragment();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, feedbackFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
