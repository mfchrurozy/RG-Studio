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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin, btnAdminLogin;
    private TextView tvRegisterLink, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginUser(false));
        btnAdminLogin.setOnClickListener(v -> loginUser(true));

        tvRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser(boolean isAdminLogin) {
        String email = etEmail.getText() != null ?
                etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ?
                etPassword.getText().toString().trim() : "";

        if (email.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            etPassword.requestFocus();
            return;
        }

        showProgress(true);

        firebaseHelper.loginUser(email, password, task -> {
            showProgress(false);

            if (task.isSuccessful()) {
                FirebaseUser user = firebaseHelper.getCurrentUser();
                if (user != null) {
                    checkRoleAndRedirect(user.getUid(), isAdminLogin);
                }
            } else {
                String error = task.getException() != null ?
                        task.getException().getMessage() : "Login gagal";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkRoleAndRedirect(String uid, boolean isAdminLogin) {
        firebaseHelper.getUserRole(uid, role -> {
            if (isAdminLogin && !"admin".equals(role)) {
                Toast.makeText(this,
                        "Akun ini bukan admin. Gunakan login biasa.",
                        Toast.LENGTH_LONG).show();
                firebaseHelper.logout();
                return;
            }

            if ("admin".equals(role)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, UserDashboardActivity.class));
            }
            finish();
        });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnAdminLogin.setEnabled(!show);
    }
}
