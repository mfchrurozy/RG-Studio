package com.rgstudio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class UserDashboardActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvWelcomeUser, tvUserEmail;
    private MaterialCardView cardListApk, cardSearchApk, cardProfile, cardUserLogout;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        firebaseHelper = FirebaseHelper.getInstance();

        if (!firebaseHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupClickListeners();
        loadUserInfo();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcomeUser = findViewById(R.id.tvWelcomeUser);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        cardListApk = findViewById(R.id.cardListApk);
        cardSearchApk = findViewById(R.id.cardSearchApk);
        cardProfile = findViewById(R.id.cardProfile);
        cardUserLogout = findViewById(R.id.cardUserLogout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dashboard");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupClickListeners() {
        cardListApk.setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadsActivity.class));
        });

        cardSearchApk.setOnClickListener(v -> {
            Intent intent = new Intent(this, DownloadsActivity.class);
            intent.putExtra("focus_search", true);
            startActivity(intent);
        });

        cardProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        cardUserLogout.setOnClickListener(v -> {
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

    private void loadUserInfo() {
        if (firebaseHelper.getCurrentUser() != null) {
            String uid = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserData(uid, document -> {
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    if (name != null) {
                        tvWelcomeUser.setText("Selamat Datang, " + name + "!");
                    }
                }
            });
            tvUserEmail.setText(firebaseHelper.getCurrentUser().getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
