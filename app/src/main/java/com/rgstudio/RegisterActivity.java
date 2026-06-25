package com.rgstudio;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFirstName, etLastName, etUsername,
            etEmail, etPassword, etPhone;
    private MaterialButton btnRegister;
    private TextView tvLoginLink;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etPhone = findViewById(R.id.etPhone);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String firstName = getText(etFirstName);
        String lastName = getText(etLastName);
        String username = getText(etUsername);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String phone = getText(etPhone);

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("Nama depan tidak boleh kosong");
            etFirstName.requestFocus();
            return;
        }

        if (username.isEmpty()) {
            etUsername.setError("Username tidak boleh kosong");
            etUsername.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Nomor handphone tidak boleh kosong");
            etPhone.requestFocus();
            return;
        }

        String fullName = firstName + " " + lastName;

        showProgress(true);

        firebaseHelper.registerUser(email, password, task -> {
            if (task.isSuccessful()) {
                String uid = firebaseHelper.getCurrentUser().getUid();

                firebaseHelper.createUserProfile(uid, email, fullName,
                        username, phone, "user",
                        aVoid -> {
                            showProgress(false);
                            Toast.makeText(this,
                                    "Registrasi berhasil! Silakan login.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        },
                        e -> {
                            showProgress(false);
                            Toast.makeText(this,
                                    "Gagal menyimpan profil: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
            } else {
                showProgress(false);
                String error = task.getException() != null ?
                        task.getException().getMessage() : "Registrasi gagal";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ?
                editText.getText().toString().trim() : "";
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
    }
}
