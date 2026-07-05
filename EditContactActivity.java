package com.example.hershield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class EditContactActivity extends AppCompatActivity {

    EditText fname,lname,phone;
    MaterialButton btnUpdate;
    int contactId;
    ContactDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_contact);

        Intent i = getIntent();
        fname = findViewById(R.id.tvFname);
        lname = findViewById(R.id.tvLname);
        phone = findViewById(R.id.tvPhone);
        contactId = i.getIntExtra("id",-1);
        fname.setText(i.getStringExtra("fname"));
        lname.setText(i.getStringExtra("lname"));
        phone.setText(i.getStringExtra("phone"));
        btnUpdate = findViewById(R.id.B1);
        dbHelper = new ContactDBHelper(this);

        btnUpdate.setOnClickListener(v -> {
            if(fname.getText().length()>0 && lname.getText().length()>0 && phone.getText().length()>0 && phone.getText().length()<11) {
                String newF = fname.getText().toString();
                String newL = lname.getText().toString();
                String newP = phone.getText().toString();

                boolean updated = dbHelper.updateContact(contactId, newF, newL, newP);
                if (updated) {
                    Toast.makeText(this, "Contact Updated Successfully!", Toast.LENGTH_SHORT).show();

                    // Redirect to ManageContacts
                    Intent intent = new Intent(this, BaseActivity.class);
                    intent.putExtra("openFragment", "manage_contacts");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(this, "Please fill data in all fields", Toast.LENGTH_SHORT).show();
            }
        });
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}