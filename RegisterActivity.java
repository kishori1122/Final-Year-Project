package com.example.hershield;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtName, edtEmail, edtMobile, edtPassword, edtConfirmPassword;
    TextView loginRedirect;
    Button btnRegister;
    ProfileDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // EdgeToEdge support
        setContentView(R.layout.activity_register);

        // Initialize Views
        edtName = findViewById(R.id.editTextName);
        edtEmail = findViewById(R.id.editTextEmail);
        edtMobile = findViewById(R.id.editTextMobile);
        edtPassword = findViewById(R.id.editTextPassword);
        edtConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        loginRedirect=findViewById(R.id.loginRedirect);
        loginRedirect.setOnClickListener(v->{
            Intent i = new Intent(this,loginactivity.class);
            startActivity(i);
        });

        dbHelper = new ProfileDBHelper(this);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String mobile = edtMobile.getText().toString().trim();
            String password = edtPassword.getText().toString();
            String confirmPassword = edtConfirmPassword.getText().toString();

            // Simple validation
            if (name.isEmpty() || email.isEmpty() || mobile.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert profile into DB
            boolean inserted = dbHelper.addProfile(name, email, mobile, password);
            if (inserted) {
                Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                finish();
                Intent i = new Intent(this,loginactivity.class);
                startActivity(i);
            } else {
                Toast.makeText(this, "Registration Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
