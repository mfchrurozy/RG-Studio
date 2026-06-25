package com.rgstudio;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddApkActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etVersion, etSize, etDescription,
            etThumbnail, etDownloadUrl, etCategory;
    private MaterialButton btnSave;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_apk);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etApkTitle);
        etVersion = findViewById(R.id.etApkVersion);
        etSize = findViewById(R.id.etApkSize);
        etDescription = findViewById(R.id.etApkDescription);
        etThumbnail = findViewById(R.id.etApkThumbnail);
        etDownloadUrl = findViewById(R.id.etApkDownloadUrl);
        etCategory = findViewById(R.id.etApkCategory);
        btnSave = findViewById(R.id.btnSaveApk);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tambah APK");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveApk());
    }

    private void saveApk() {
        String title = getText(etTitle);
        String version = getText(etVersion);
        String size = getText(etSize);
        String description = getText(etDescription);
        String thumbnail = getText(etThumbnail);
        String downloadUrl = getText(etDownloadUrl);
        String category = getText(etCategory);

        // Validation
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
        if (size.isEmpty()) {
            etSize.setError("Ukuran tidak boleh kosong");
            etSize.requestFocus();
            return;
        }
        if (description.isEmpty()) {
            etDescription.setError("Deskripsi tidak boleh kosong");
            etDescription.requestFocus();
            return;
        }
        if (downloadUrl.isEmpty()) {
            etDownloadUrl.setError("Link download tidak boleh kosong");
            etDownloadUrl.requestFocus();
            return;
        }
        if (category.isEmpty()) {
            etCategory.setError("Kategori tidak boleh kosong");
            etCategory.requestFocus();
            return;
        }

        String dateCreated = new SimpleDateFormat("dd MMM yyyy",
                Locale.getDefault()).format(new Date());

        ApkModel apk = new ApkModel();
        apk.setTitle(title);
        apk.setVersion(version);
        apk.setSize(size);
        apk.setDescription(description);
        apk.setImageUrl(thumbnail);
        apk.setDownloadUrl(downloadUrl);
        apk.setCategory(category);
        apk.setDateCreated(dateCreated);

        showProgress(true);

        firebaseHelper.addApk(apk,
                documentReference -> {
                    showProgress(false);
                    Toast.makeText(this, "APK berhasil ditambahkan!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    showProgress(false);
                    Toast.makeText(this,
                            "Gagal menambahkan: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ?
                editText.getText().toString().trim() : "";
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
