package com.rgstudio;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditApkActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etVersion, etSize, etDescription,
            etThumbnail, etDownloadUrl, etCategory;
    private MaterialButton btnUpdate;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private FirebaseHelper firebaseHelper;
    private String apkId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_apk);

        firebaseHelper = FirebaseHelper.getInstance();
        apkId = getIntent().getStringExtra("apk_id");

        initViews();
        setupToolbar();
        setupListeners();
        loadExistingData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etEditTitle);
        etVersion = findViewById(R.id.etEditVersion);
        etSize = findViewById(R.id.etEditSize);
        etDescription = findViewById(R.id.etEditDescription);
        etThumbnail = findViewById(R.id.etEditThumbnail);
        etDownloadUrl = findViewById(R.id.etEditDownloadUrl);
        etCategory = findViewById(R.id.etEditCategory);
        btnUpdate = findViewById(R.id.btnUpdateApk);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit APK");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(v -> updateApk());
    }

    private void loadExistingData() {
        ApkModel apk = (ApkModel) getIntent().getSerializableExtra("apk_data");

        if (apk != null) {
            etTitle.setText(apk.getTitle());
            etVersion.setText(apk.getVersion());
            etSize.setText(apk.getSize());
            etDescription.setText(apk.getDescription());
            etThumbnail.setText(apk.getImageUrl());
            etDownloadUrl.setText(apk.getDownloadUrl());
            etCategory.setText(apk.getCategory());
        } else if (apkId != null) {
            firebaseHelper.getApkDetail(apkId, loadedApk -> {
                if (loadedApk != null) {
                    etTitle.setText(loadedApk.getTitle());
                    etVersion.setText(loadedApk.getVersion());
                    etSize.setText(loadedApk.getSize());
                    etDescription.setText(loadedApk.getDescription());
                    etThumbnail.setText(loadedApk.getImageUrl());
                    etDownloadUrl.setText(loadedApk.getDownloadUrl());
                    etCategory.setText(loadedApk.getCategory());
                }
            });
        }
    }

    private void updateApk() {
        String title = getText(etTitle);
        String version = getText(etVersion);
        String size = getText(etSize);
        String description = getText(etDescription);
        String thumbnail = getText(etThumbnail);
        String downloadUrl = getText(etDownloadUrl);
        String category = getText(etCategory);

        if (title.isEmpty()) {
            etTitle.setError("Judul tidak boleh kosong");
            etTitle.requestFocus();
            return;
        }
        if (version.isEmpty()) {
            etVersion.setError("Versi tidak boleh kosong");
            etVersion.requestFocus();
            return;
        }
        if (downloadUrl.isEmpty()) {
            etDownloadUrl.setError("Link download tidak boleh kosong");
            etDownloadUrl.requestFocus();
            return;
        }

        ApkModel apk = new ApkModel();
        apk.setTitle(title);
        apk.setVersion(version);
        apk.setSize(size);
        apk.setDescription(description);
        apk.setImageUrl(thumbnail);
        apk.setDownloadUrl(downloadUrl);
        apk.setCategory(category);

        showProgress(true);

        firebaseHelper.updateApk(apkId, apk,
                aVoid -> {
                    showProgress(false);
                    Toast.makeText(this, "APK berhasil diperbarui!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    showProgress(false);
                    Toast.makeText(this,
                            "Gagal memperbarui: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ?
                editText.getText().toString().trim() : "";
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnUpdate.setEnabled(!show);
    }
}
