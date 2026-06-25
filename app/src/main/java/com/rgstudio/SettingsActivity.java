package com.rgstudio;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDarkMode, switchNotification,
            switchSound, switchAutoDownload;
    private LinearLayout layoutFontSize, layoutClearCache,
            layoutPrivacy, layoutTerms;
    private TextView tvFontSizeValue, tvCacheSize, tvAppVersion;
    private Toolbar toolbar;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "rg_studio_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotification = findViewById(R.id.switchNotification);
        switchSound = findViewById(R.id.switchSound);
        switchAutoDownload = findViewById(R.id.switchAutoDownload);

        layoutFontSize = findViewById(R.id.layoutFontSize);
        layoutClearCache = findViewById(R.id.layoutClearCache);
        layoutPrivacy = findViewById(R.id.layoutPrivacy);
        layoutTerms = findViewById(R.id.layoutTerms);

        tvFontSizeValue = findViewById(R.id.tvFontSizeValue);
        tvCacheSize = findViewById(R.id.tvCacheSize);
        tvAppVersion = findViewById(R.id.tvAppVersion);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pengaturan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadSettings() {
        // Load Dark Mode
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        // Load Notification
        boolean isNotification = prefs.getBoolean("notification", true);
        switchNotification.setChecked(isNotification);

        // Load Sound
        boolean isSound = prefs.getBoolean("chat_sound", true);
        switchSound.setChecked(isSound);

        // Load Auto Download
        boolean isAutoDownload = prefs.getBoolean("auto_download", true);
        switchAutoDownload.setChecked(isAutoDownload);

        // Load Font Size
        int fontSizeIndex = prefs.getInt("font_size", 1); // 0=Kecil, 1=Normal, 2=Besar
        String[] fontSizes = {"Kecil", "Normal", "Besar"};
        tvFontSizeValue.setText(fontSizes[fontSizeIndex]);

        // Load Cache Size
        calculateCacheSize();

        // App Version
        try {
            String version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText(version != null ? version : "1.0.0");
        } catch (Exception e) {
            tvAppVersion.setText("1.0.0");
        }
    }

    private void setupListeners() {

        // ====== DARK MODE ======
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO);
            }

            Toast.makeText(this,
                    isChecked ? "Mode gelap diaktifkan" : "Mode terang diaktifkan",
                    Toast.LENGTH_SHORT).show();
        });

        // ====== NOTIFICATION ======
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notification", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Notifikasi diaktifkan" : "Notifikasi dinonaktifkan",
                    Toast.LENGTH_SHORT).show();
        });

        // ====== CHAT SOUND ======
        switchSound.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("chat_sound", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Suara chat diaktifkan" : "Suara chat dinonaktifkan",
                    Toast.LENGTH_SHORT).show();
        });

        // ====== AUTO DOWNLOAD ======
        switchAutoDownload.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("auto_download", isChecked).apply();
            Toast.makeText(this,
                    isChecked ? "Auto download via WiFi aktif" : "Auto download nonaktif",
                    Toast.LENGTH_SHORT).show();
        });

        // ====== FONT SIZE ======
        layoutFontSize.setOnClickListener(v -> showFontSizeDialog());

        // ====== CLEAR CACHE ======
        layoutClearCache.setOnClickListener(v -> showClearCacheDialog());

        // ====== PRIVACY POLICY ======
        layoutPrivacy.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Kebijakan Privasi")
                    .setMessage(
                            "Kebijakan Privasi RG Studio\n\n" +
                            "1. Data Pribadi\n" +
                            "Kami mengumpulkan data yang Anda berikan saat " +
                            "mendaftar (nama, email, nomor HP) untuk keperluan " +
                            "autentikasi dan layanan.\n\n" +
                            "2. Penggunaan Data\n" +
                            "Data hanya digunakan untuk menyediakan dan " +
                            "meningkatkan layanan RG Studio.\n\n" +
                            "3. Penyimpanan Data\n" +
                            "Data disimpan secara aman di Firebase dengan " +
                            "enkripsi standar industri.\n\n" +
                            "4. Berbagi Data\n" +
                            "Kami tidak menjual atau membagikan data pribadi " +
                            "Anda kepada pihak ketiga tanpa izin.\n\n" +
                            "5. Hak Pengguna\n" +
                            "Anda berhak meminta penghapusan data Anda " +
                            "kapan saja dengan menghubungi kami.\n\n" +
                            "© 2026 RG Studio")
                    .setPositiveButton("OK", null)
                    .show();
        });

        // ====== TERMS ======
        layoutTerms.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Syarat & Ketentuan")
                    .setMessage(
                            "Syarat & Ketentuan RG Studio\n\n" +
                            "1. Penerimaan Syarat\n" +
                            "Dengan menggunakan aplikasi ini, Anda menyetujui " +
                            "seluruh syarat dan ketentuan yang berlaku.\n\n" +
                            "2. Akun Pengguna\n" +
                            "Anda bertanggung jawab menjaga kerahasiaan " +
                            "akun dan password Anda.\n\n" +
                            "3. Konten\n" +
                            "Anda dilarang mengunggah konten yang melanggar " +
                            "hukum, mengandung SARA, atau merugikan pihak lain.\n\n" +
                            "4. Layanan\n" +
                            "RG Studio berhak mengubah, menangguhkan, atau " +
                            "menghentikan layanan kapan saja.\n\n" +
                            "5. Pembatasan\n" +
                            "Penggunaan aplikasi hanya untuk tujuan yang sah " +
                            "dan sesuai dengan hukum yang berlaku.\n\n" +
                            "© 2026 RG Studio")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    private void showFontSizeDialog() {
        String[] fontSizes = {"Kecil", "Normal", "Besar"};
        int currentIndex = prefs.getInt("font_size", 1);

        new AlertDialog.Builder(this)
                .setTitle("Pilih Ukuran Font")
                .setSingleChoiceItems(fontSizes, currentIndex, (dialog, which) -> {
                    prefs.edit().putInt("font_size", which).apply();
                    tvFontSizeValue.setText(fontSizes[which]);
                    Toast.makeText(this,
                            "Ukuran font diubah ke: " + fontSizes[which],
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Cache")
                .setMessage("Apakah Anda yakin ingin menghapus semua cache? " +
                        "Ini tidak akan menghapus data akun Anda.")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    clearCache();
                    calculateCacheSize();
                    Toast.makeText(this, "Cache berhasil dibersihkan!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void clearCache() {
        try {
            File cacheDir = getCacheDir();
            deleteDir(cacheDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        return dir != null && dir.delete();
    }

    private void calculateCacheSize() {
        try {
            File cacheDir = getCacheDir();
            long size = getDirSize(cacheDir);
            String sizeStr = formatSize(size);
            tvCacheSize.setText("Ukuran cache: " + sizeStr);
        } catch (Exception e) {
            tvCacheSize.setText("Ukuran cache: 0 B");
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }

    private String formatSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024)
            return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }
}
