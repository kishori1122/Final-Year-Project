package com.example.hershield;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class FeedbackFragment extends Fragment {

    private EditText nameEditText, messageEditText;
    private RatingBar ratingBar;
    private Button submitBtn;

    public FeedbackFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        nameEditText = view.findViewById(R.id.feedback_name);
        messageEditText = view.findViewById(R.id.feedback_message);
        ratingBar = view.findViewById(R.id.feedback_rating);
        submitBtn = view.findViewById(R.id.submit_feedback);

        submitBtn.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String message = messageEditText.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(message)) {
                messageEditText.setError("Message is required");
                messageEditText.requestFocus();
                return;
            }

            if (rating == 0) {
                Toast.makeText(getActivity(), "Please provide a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String subject = "App Feedback from " + name;
                String body = "Rating: " + rating + "\n\nMessage:\n" + message;

                String uriText = "mailto:sheshield.support@gmail.com" +
                        "?subject=" + Uri.encode(subject) +
                        "&body=" + Uri.encode(body);

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(uriText));

                startActivity(Intent.createChooser(emailIntent, "Send Feedback via"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
