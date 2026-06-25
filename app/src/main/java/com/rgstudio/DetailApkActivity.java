package com.rgstudio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;

public class DetailApkActivity extends AppCompatActivity {

    private ImageView ivThumbnail;
    private TextView tvTitle, tvVersion, tvSize, tvCategory,
            tvDescription, tvDate;
    private MaterialButton btnDownload;
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    private String downloadUrl;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_apk);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupToolbar();
        loadApkData();
    }

    private void initViews() {
        ivThumbnail = findViewById(R.id.ivDetailThumbnail);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvVersion = findViewById(R.id.tvDetailVersion);
        tvSize = findViewById(R.id.tvDetailSize);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvDescription = findViewById(R.id.tvDetailDescription);
        tvDate = findViewById(R.id.tvDetailDate);
        btnDownload = findViewById(R.id.btnDetailDownload);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadApkData() {
        String apkId = getIntent().getStringExtra("apk_id");
        ApkModel apk = (ApkModel) getIntent().getSerializableExtra("apk_data");

        if (apk != null) {
            displayApk(apk);
        } else if (apkId != null) {
            firebaseHelper.getApkDetail(apkId, loadedApk -> {
                if (loadedApk != null) {
                    displayApk(loadedApk);
                } else {
                    Toast.makeText(this, "Data tidak ditemukan",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }

    private void displayApk(ApkModel apk) {
        collapsingToolbar.setTitle(apk.getTitle());

        tvTitle.setText(apk.getTitle());
        tvVersion.setText("v" + apk.getVersion());
        tvSize.setText(apk.getSize());
        tvCategory.setText(apk.getCategory());
        tvDescription.setText(apk.getDescription());
        tvDate.setText(apk.getDateCreated());

        downloadUrl = apk.getDownloadUrl();

        // Load thumbnail
        if (apk.getImageUrl() != null && !apk.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(apk.getImageUrl())
                    .placeholder(R.drawable.logo_placeholder)
                    .error(R.drawable.logo_placeholder)
                    .centerCrop()
                    .into(ivThumbnail);
        }

        btnDownload.setOnClickListener(v -> {
            if (downloadUrl != null && !downloadUrl.isEmpty()) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(downloadUrl));
                startActivity(browserIntent);
            } else {
                Toast.makeText(this, "Link download tidak tersedia",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
