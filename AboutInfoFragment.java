package com.example.hershield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AboutInfoFragment extends Fragment {

    public AboutInfoFragment() {
    }

    public static AboutInfoFragment newInstance() {
        AboutInfoFragment fragment = new AboutInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about_info, container, false);

        // Rate our App button -> open FeedbackFragment
        Button feedbackBtn = view.findViewById(R.id.feedbackBtn);
        feedbackBtn.setOnClickListener(v -> {
            Log.d("msg", "Rate Click");
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new FeedbackFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });



        // ✅ Return the correct view
        return view;
    }
}