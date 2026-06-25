package com.rgstudio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class AdminDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvAdminEmail;
    private MaterialCardView cardManageApk, cardAddApk, cardManageUsers, cardLogout;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        firebaseHelper = FirebaseHelper.getInstance();

        // Check admin access
        if (!firebaseHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        loadAdminInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        cardManageApk = findViewById(R.id.cardManageApk);
        cardAddApk = findViewById(R.id.cardAddApk);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        cardManageApk.setOnClickListener(v -> {
            Intent intent = new Intent(this, DownloadsActivity.class);
            intent.putExtra("admin_mode", true);
            startActivity(intent);
        });

        cardAddApk.setOnClickListener(v -> {
            startActivity(new Intent(this, AddApkActivity.class));
        });

        cardManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Fitur kelola user segera hadir!",
                    Toast.LENGTH_SHORT).show();
        });

        cardLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Apakah Anda yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        firebaseHelper.logout();
                        startActivity(new Intent(this, LoginActivity.class));
                        finishAffinity();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    private void loadAdminInfo() {
        if (firebaseHelper.getCurrentUser() != null) {
            tvAdminEmail.setText(firebaseHelper.getCurrentUser().getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
