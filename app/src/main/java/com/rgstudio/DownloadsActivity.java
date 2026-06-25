package com.rgstudio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DownloadsActivity extends AppCompatActivity
        implements ApkAdapter.OnApkClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private FloatingActionButton fabAdd;
    private Toolbar toolbar;

    private ApkAdapter adapter;
    private List<ApkModel> apkList;
    private FirebaseHelper firebaseHelper;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        firebaseHelper = FirebaseHelper.getInstance();
        isAdmin = getIntent().getBooleanExtra("admin_mode", false);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        checkAdminAndShowFab();
        loadApkList();

        // Handle search query from intent
        String searchQuery = getIntent().getStringExtra("search_query");
        if (searchQuery != null && !searchQuery.isEmpty()) {
            // Will filter after loading
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyState = findViewById(R.id.emptyState);
        fabAdd = findViewById(R.id.fabAdd);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Unduhan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        apkList = new ArrayList<>();
        adapter = new ApkAdapter(this, apkList, isAdmin, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.primary_blue);
        swipeRefresh.setOnRefreshListener(this::loadApkList);
    }

    private void checkAdminAndShowFab() {
        if (isAdmin && firebaseHelper.isLoggedIn()) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> {
                startActivity(new Intent(this, AddApkActivity.class));
            });
        } else if (firebaseHelper.isLoggedIn()) {
            // Check if current user is admin
            firebaseHelper.getUserRole(
                    firebaseHelper.getCurrentUser().getUid(),
                    role -> {
                        if ("admin".equals(role)) {
                            isAdmin = true;
                            fabAdd.setVisibility(View.VISIBLE);
                            fabAdd.setOnClickListener(v -> {
                                startActivity(new Intent(this, AddApkActivity.class));
                            });
                            adapter = new ApkAdapter(this, apkList, true, this);
                            recyclerView.setAdapter(adapter);
                        }
                    });
        }
    }

    private void loadApkList() {
        swipeRefresh.setRefreshing(true);

        firebaseHelper.getDownloadsCollection()
                .get()
                .addOnCompleteListener(task -> {
                    swipeRefresh.setRefreshing(false);

                    if (task.isSuccessful()) {
                        QuerySnapshot snapshot = task.getResult();
                        apkList.clear();

                        if (snapshot != null && !snapshot.isEmpty()) {
                            snapshot.forEach(doc -> {
                                ApkModel apk = doc.toObject(ApkModel.class);
                                apk.setId(doc.getId());
                                apkList.add(apk);
                            });
                            emptyState.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                        adapter.updateList(apkList);

                        // Apply search filter if needed
                        String searchQuery = getIntent().getStringExtra("search_query");
                        if (searchQuery != null && !searchQuery.isEmpty()) {
                            filterList(searchQuery);
                        }
                    } else {
                        Toast.makeText(this,
                                "Gagal memuat data: " +
                                        (task.getException() != null ?
                                                task.getException().getMessage() : ""),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void filterList(String query) {
        List<ApkModel> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ApkModel apk : apkList) {
            if ((apk.getTitle() != null &&
                    apk.getTitle().toLowerCase().contains(lowerQuery)) ||
                    (apk.getCategory() != null &&
                            apk.getCategory().toLowerCase().contains(lowerQuery))) {
                filteredList.add(apk);
            }
        }

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
        } else {
            emptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Cari APK...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    adapter.updateList(apkList);
                } else {
                    filterList(newText);
                }
                return true;
            }
        });

        // Auto-expand search if requested
        if (getIntent().getBooleanExtra("focus_search", false)) {
            searchItem.expandActionView();
        }

        return true;
    }

    // ===================== ApkAdapter Click Listeners =====================

    @Override
    public void onDetailClick(ApkModel apk) {
        Intent intent = new Intent(this, DetailApkActivity.class);
        intent.putExtra("apk_id", apk.getId());
        intent.putExtra("apk_data", apk);
        startActivity(intent);
    }

    @Override
    public void onDownloadClick(ApkModel apk) {
        if (apk.getDownloadUrl() != null && !apk.getDownloadUrl().isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(apk.getDownloadUrl()));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "Link download tidak tersedia",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEditClick(ApkModel apk) {
        Intent intent = new Intent(this, EditApkActivity.class);
        intent.putExtra("apk_id", apk.getId());
        intent.putExtra("apk_data", apk);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(ApkModel apk) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus APK")
                .setMessage("Apakah Anda yakin ingin menghapus \"" +
                        apk.getTitle() + "\"?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    firebaseHelper.deleteApk(apk.getId(),
                            aVoid -> {
                                Toast.makeText(this, "APK berhasil dihapus",
                                        Toast.LENGTH_SHORT).show();
                                loadApkList();
                            },
                            e -> {
                                Toast.makeText(this,
                                        "Gagal menghapus: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadApkList();
    }
}
