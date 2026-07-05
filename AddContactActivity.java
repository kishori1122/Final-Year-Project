package com.example.hershield;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class AddContactActivity extends AppCompatActivity {

    MaterialButton btnSave;
    EditText edtFName, edtLName, edtPhone;
    ContactDBHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_contact);

        edtFName = findViewById(R.id.f1);
        edtLName = findViewById(R.id.L1);
        edtPhone = findViewById(R.id.p1);
        btnSave = findViewById(R.id.B1);
        dbHelper = new ContactDBHelper(this);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fname = edtFName.getText().toString().trim();
                String lname = edtLName.getText().toString().trim();
                String contact = edtPhone.getText().toString().trim();

                // ✅ Validate input
                if (fname.isEmpty() || lname.isEmpty() || contact.isEmpty()) {
                    Toast.makeText(AddContactActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ✅ Insert into database
                boolean inserted = dbHelper.addToContacts(fname, lname, contact);

                if (inserted) {
                    Toast.makeText(AddContactActivity.this, "Contact Added Successfully!", Toast.LENGTH_SHORT).show();
                    // Clear fields
                    edtFName.setText("");
                    edtLName.setText("");
                    edtPhone.setText("");
                    Intent intent = new Intent(AddContactActivity.this, BaseActivity.class);
                    intent.putExtra("openFragment", "manage_contacts"); // optional flag
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddContactActivity.this, "Failed to Add Contact", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}