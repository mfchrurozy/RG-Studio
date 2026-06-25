package com.rgstudio;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvName, tvEmail, tvUsername, tvPhone, tvRole;
    private MaterialButton btnLogout;
    private Toolbar toolbar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseHelper = FirebaseHelper.getInstance();

        if (!firebaseHelper.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupListeners();
        loadProfileData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatar = findViewById(R.id.ivProfileAvatar);
        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvUsername = findViewById(R.id.tvProfileUsername);
        tvPhone = findViewById(R.id.tvProfilePhone);
        tvRole = findViewById(R.id.tvProfileRole);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Apakah Anda yakin ingin logout?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        firebaseHelper.logout();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finishAffinity();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        });
    }

    private void loadProfileData() {
        String uid = firebaseHelper.getCurrentUser().getUid();
        String email = firebaseHelper.getCurrentUser().getEmail();
        tvEmail.setText(email);

        firebaseHelper.getUserData(uid, document -> {
            if (document != null && document.exists()) {
                String name = document.getString("name");
                String username = document.getString("username");
                String phone = document.getString("phone");
                String role = document.getString("role");

                tvName.setText(name != null ? name : "User");
                tvUsername.setText(username != null ? username : "-");
                tvPhone.setText(phone != null ? phone : "-");
                tvRole.setText(role != null ? role : "user");
            }
        });
    }
}
