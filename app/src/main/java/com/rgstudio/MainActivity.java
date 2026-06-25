package com.rgstudio;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private SearchView searchView;
    private CardView cardDownloads, cardUatc, cardAccount;
    private TextView tvNavName, tvNavEmail;

    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseHelper = FirebaseHelper.getInstance();

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupClickListeners();
        updateNavHeader();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        cardDownloads = findViewById(R.id.cardDownloads);
        cardUatc = findViewById(R.id.cardUatc);
        cardAccount = findViewById(R.id.cardAccount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("RG Studio");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupClickListeners() {
        cardDownloads.setOnClickListener(v -> {
            startActivity(new Intent(this, DownloadsActivity.class));
        });

        cardUatc.setOnClickListener(v -> {
            launchUATC();
        });

        cardAccount.setOnClickListener(v -> {
            if (firebaseHelper.isLoggedIn()) {
                checkUserRole();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, DownloadsActivity.class);
                intent.putExtra("search_query", query);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        tvNavName = headerView.findViewById(R.id.tvNavName);
        tvNavEmail = headerView.findViewById(R.id.tvNavEmail);

        if (firebaseHelper.isLoggedIn()) {
            firebaseHelper.getUserData(firebaseHelper.getCurrentUser().getUid(),
                    document -> {
                        if (document != null && document.exists()) {
                            String name = document.getString("name");
                            String email = document.getString("email");
                            tvNavName.setText(name != null ? name : "User");
                            tvNavEmail.setText(email != null ? email :
                                    firebaseHelper.getCurrentUser().getEmail());
                        }
                    });
        } else {
            tvNavName.setText("RG Studio");
            tvNavEmail.setText("Silakan login");
        }
    }

    private void launchUATC() {
        try {
            Intent launchIntent = getPackageManager()
                    .getLaunchIntentForPackage("com.vector3d.uatc");
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                Toast.makeText(this,
                        "UATC belum terinstall. Silakan install terlebih dahulu.",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Gagal meluncurkan UATC: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void checkUserRole() {
        firebaseHelper.getUserRole(firebaseHelper.getCurrentUser().getUid(), role -> {
            if ("admin".equals(role)) {
                startActivity(new Intent(this, AdminDashboardActivity.class));
            } else {
                startActivity(new Intent(this, UserDashboardActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home
        } else if (id == R.id.nav_uatc) {
            launchUATC();
        } else if (id == R.id.nav_downloads) {
            startActivity(new Intent(this, DownloadsActivity.class));
        } else if (id == R.id.nav_account) {
            if (firebaseHelper.isLoggedIn()) {
                checkUserRole();
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
          } else if (id == R.id.nav_nimbrung) {
        // ← UBAH BAGIAN INI
        startActivity(new Intent(this, NimbrungActivity.class));
    } else if (id == R.id.nav_settings) {
        // ← UBAH BAGIAN INI
        startActivity(new Intent(this, SettingsActivity.class));
    } else if (id == R.id.nav_privacy) {
        showPrivacyPolicy();
    } else if (id == R.id.nav_about) {
        showAbout();
    }

    drawerLayout.closeDrawer(GravityCompat.START);
    return true;
}
    private void showPrivacyPolicy() {
        new AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("RG Studio menghormati privasi Anda. " +
                        "Data pribadi yang dikumpulkan hanya digunakan untuk " +
                        "meningkatkan layanan kami. Kami tidak menjual atau " +
                        "membagikan data Anda kepada pihak ketiga tanpa izin.\n\n" +
                        "© 2026 RG Studio. All rights reserved.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showAbout() {
        new AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage("RG Studio v1.0\n\n" +
                        "Aplikasi ini dikembangkan oleh RG Studio.\n" +
                        "Menyediakan berbagai aplikasi dan tools " +
                        "untuk kebutuhan digital Anda.\n\n" +
                        "© 2026 RG Studio. All rights reserved.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
    }
}
