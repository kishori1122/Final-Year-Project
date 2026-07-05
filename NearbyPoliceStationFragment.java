package com.example.hershield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class NearbyPoliceStationFragment extends Fragment {


    public NearbyPoliceStationFragment() {
    }

    public static NearbyPoliceStationFragment newInstance(String param1, String param2) {
        NearbyPoliceStationFragment fragment = new NearbyPoliceStationFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_police_station, container, false);

        MaterialButton searchOnMap = view.findViewById(R.id.searchonmap);

        searchOnMap.setOnClickListener(v -> {
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=police stations near me");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Google Maps not installed!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
