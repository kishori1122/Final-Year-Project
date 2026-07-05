package com.example.hershield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.hershield.R;

public class SafetyTipsFraGment extends Fragment {

    private LinearLayout sectionPersonal, sectionTravel, sectionOnline;
    private Button btnPersonal, btnTravel, btnOnline;
    private Button btnSOS, btnMap, btnVideos;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_safety_tips, container, false);

        sectionPersonal = view.findViewById(R.id.sectionPersonal);
        sectionTravel   = view.findViewById(R.id.sectionTravel);
        sectionOnline   = view.findViewById(R.id.sectionOnline);

        btnPersonal = view.findViewById(R.id.btnPersonal);
        btnTravel   = view.findViewById(R.id.btnTravel);
        btnOnline   = view.findViewById(R.id.btnOnline);

        btnSOS       = view.findViewById(R.id.btnSos);
        btnMap       = view.findViewById(R.id.btnMap);
        btnVideos    = view.findViewById(R.id.btnVideos);

        showAllSections();

        btnPersonal.setOnClickListener(v -> showOnly(sectionPersonal));
        btnTravel.setOnClickListener(v -> showOnly(sectionTravel));
        btnOnline.setOnClickListener(v -> showOnly(sectionOnline));

        btnSOS.setOnClickListener(v -> {
            Fragment homeFragment = new HomeFragment();
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, homeFragment); // use your actual container id
            transaction.addToBackStack(null); // optional, so user can go back
            transaction.commit();
        });
        btnMap.setOnClickListener(v -> {

            String query = "police station OR hospital OR NGO near me";
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(query));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Google Maps not installed!", Toast.LENGTH_SHORT).show();
            }
        });

        btnVideos.setOnClickListener(v -> {
            try {
                String query = "Self Defence for Women";
                String url = "https://www.youtube.com/results?search_query=" + Uri.encode(query);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.google.android.youtube");
                startActivity(intent);

            } catch (Exception e) {
                String query = "Safety Tips & Self Defence for Women";
                String url = "https://www.youtube.com/results?search_query=" + Uri.encode(query);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });


        return view;
    }

    private void showOnly(LinearLayout section) {
        sectionPersonal.setVisibility(section == sectionPersonal ? View.VISIBLE : View.GONE);
        sectionTravel.setVisibility(section   == sectionTravel   ? View.VISIBLE : View.GONE);
        sectionOnline.setVisibility(section   == sectionOnline   ? View.VISIBLE : View.GONE);
    }

    private void showAllSections() {
        sectionPersonal.setVisibility(View.VISIBLE);
        sectionTravel.setVisibility(View.VISIBLE);
        sectionOnline.setVisibility(View.VISIBLE);
    }
}