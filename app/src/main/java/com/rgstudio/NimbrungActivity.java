package com.rgstudio;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.OnDisconnect;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NimbrungActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private TextInputEditText etMessage;
    private FloatingActionButton fabSend;
    private LinearLayout loginWarning, inputBar, onlineBar;
    private TextView tvOnlineCount, tvCurrentUser;
    private MaterialButton btnLoginNimbrung;
    private Toolbar toolbar;

    private ChatAdapter chatAdapter;
    private List<MessageModel> messageList;
    private FirebaseHelper firebaseHelper;

    // Firebase Realtime Database
    private FirebaseDatabase realtimeDb;
    private DatabaseReference messagesRef;
    private DatabaseReference onlineUsersRef;
    private ChildEventListener messageListener;
    private ValueEventListener onlineListener;

    private String currentUserId = "";
    private String currentUserName = "Anonim";
    private boolean playSound = true;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nimbrung);

        firebaseHelper = FirebaseHelper.getInstance();
        realtimeDb = FirebaseDatabase.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        checkLoginState();
        loadSettings();
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        fabSend = findViewById(R.id.fabSend);
        loginWarning = findViewById(R.id.loginWarning);
        inputBar = findViewById(R.id.inputBar);
        onlineBar = findViewById(R.id.onlineBar);
        tvOnlineCount = findViewById(R.id.tvOnlineCount);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);
        btnLoginNimbrung = findViewById(R.id.btnLoginNimbrung);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nimbrung");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(chatAdapter);
    }

    private void checkLoginState() {
        if (firebaseHelper.isLoggedIn()) {
            // User sudah login
            currentUserId = firebaseHelper.getCurrentUser().getUid();
            String email = firebaseHelper.getCurrentUser().getEmail();

            // Load nama dari Firestore
            firebaseHelper.getUserData(currentUserId, document -> {
                if (document != null && document.exists()) {
                    String name = document.getString("name");
                    if (name != null && !name.isEmpty()) {
                        currentUserName = name;
                    } else {
                        currentUserName = email != null ?
                                email.split("@")[0] : "User";
                    }
                } else {
                    currentUserName = email != null ?
                            email.split("@")[0] : "User";
                }
                tvCurrentUser.setText("Login sebagai: " + currentUserName);
            });

            // Update adapter dengan currentUserId
            chatAdapter = new ChatAdapter(this, messageList, currentUserId);
            rvChat.setAdapter(chatAdapter);

            // Tampilkan chat UI
            loginWarning.setVisibility(View.GONE);
            rvChat.setVisibility(View.VISIBLE);
            inputBar.setVisibility(View.VISIBLE);
            onlineBar.setVisibility(View.VISIBLE);

            // Setup Firebase Realtime listeners
            setupRealtimeListeners();
            setupPresence();
            setupSendButton();

        } else {
            // Belum login
            loginWarning.setVisibility(View.VISIBLE);
            rvChat.setVisibility(View.GONE);
            inputBar.setVisibility(View.GONE);
            onlineBar.setVisibility(View.GONE);

            btnLoginNimbrung.setOnClickListener(v -> {
                startActivity(new Intent(this, LoginActivity.class));
            });
        }
    }

    private void setupSendButton() {
        fabSend.setOnClickListener(v -> {
            String messageText = etMessage.getText() != null ?
                    etMessage.getText().toString().trim() : "";

            if (messageText.isEmpty()) {
                Toast.makeText(this, "Pesan tidak boleh kosong",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (messageText.length() > 500) {
                Toast.makeText(this, "Pesan maksimal 500 karakter",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            sendMessage(messageText);
            etMessage.setText("");
        });
    }

    private void sendMessage(String messageText) {
        DatabaseReference newMessageRef = messagesRef.push();
        String messageId = newMessageRef.getKey();

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("messageId", messageId);
        messageData.put("senderId", currentUserId);
        messageData.put("senderName", currentUserName);
        messageData.put("message", messageText);
        messageData.put("timestamp", ServerValue.TIMESTAMP);
        messageData.put("type", "text");

        newMessageRef.setValue(messageData)
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Gagal mengirim: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRealtimeListeners() {
        messagesRef = realtimeDb.getReference("nimbrung_messages");

        // Listener untuk pesan baru
        messageListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                MessageModel msg = snapshot.getValue(MessageModel.class);
                if (msg != null) {
                    msg.setMessageId(snapshot.getKey());
                    messageList.add(msg);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    rvChat.smoothScrollToPosition(messageList.size() - 1);

                    // Play sound jika pesan dari orang lain
                    if (!msg.getSenderId().equals(currentUserId) && playSound) {
                        playNotificationSound();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String key = snapshot.getKey();
                for (int i = 0; i < messageList.size(); i++) {
                    if (messageList.get(i).getMessageId() != null &&
                            messageList.get(i).getMessageId().equals(key)) {
                        messageList.remove(i);
                        chatAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {}

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NimbrungActivity.this,
                        "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        // Query 100 pesan terakhir
        messagesRef.orderByChild("timestamp")
                .limitToLast(100)
                .addChildEventListener(messageListener);

        // Listener untuk user online
        onlineUsersRef = realtimeDb.getReference("nimbrung_online");
        onlineListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int) snapshot.getChildrenCount();
                tvOnlineCount.setText(count + " pengguna online");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        onlineUsersRef.addValueEventListener(onlineListener);
    }

    private void setupPresence() {
        if (currentUserId.isEmpty()) return;

        DatabaseReference myOnlineRef = onlineUsersRef.child(currentUserId);

        // Set online status
        myOnlineRef.setValue(true);
        myOnlineRef.onDisconnect().removeValue();

        // Update last seen
        DatabaseReference myLastSeenRef = realtimeDb
                .getReference("nimbrung_last_seen")
                .child(currentUserId);
        myLastSeenRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
    }

    private void loadSettings() {
        // Load preferensi suara dari SharedPreferences
        playSound = getSharedPreferences("rg_studio_prefs", MODE_PRIVATE)
                .getBoolean("chat_sound", true);
    }

    private void playNotificationSound() {
        try {
            // Menggunakan suara notifikasi sistem
            mediaPlayer = MediaPlayer.create(this,
                    android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
            }
        } catch (Exception e) {
            // Ignore sound errors
        }
    }

    private void removeListeners() {
        if (messagesRef != null && messageListener != null) {
            messagesRef.removeEventListener(messageListener);
        }
        if (onlineUsersRef != null && onlineListener != null) {
            onlineUsersRef.removeEventListener(onlineListener);
        }
    }

    private void setOffline() {
        if (currentUserId.isEmpty()) return;
        onlineUsersRef.child(currentUserId).removeValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseHelper.isLoggedIn()) {
            setupPresence();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        setOffline();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListeners();
        setOffline();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
