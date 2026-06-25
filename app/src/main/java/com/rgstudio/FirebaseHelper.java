package com.rgstudio;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {

    private static final String TAG = "FirebaseHelper";
    private static FirebaseHelper instance;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    // ===================== AUTHENTICATION =====================

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseFirestore getFirestore() {
        return mFirestore;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void registerUser(String email, String password,
                             OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public void loginUser(String email, String password,
                          OnCompleteListener<AuthResult> onCompleteListener) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(onCompleteListener);
    }

    public void logout() {
        mAuth.signOut();
    }

    public void sendPasswordResetEmail(String email,
                                       OnCompleteListener<Void> onCompleteListener) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(onCompleteListener);
    }

    // ===================== USER MANAGEMENT =====================

    public void createUserProfile(String uid, String email, String name,
                                  String username, String phone, String role,
                                  OnSuccessListener<Void> onSuccess,
                                  OnFailureListener onFailure) {
        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("email", email);
        user.put("name", name);
        user.put("username", username);
        user.put("phone", phone);
        user.put("role", role);

        mFirestore.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getUserRole(String uid, OnRoleCallback callback) {
        mFirestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        callback.onRoleResult(role != null ? role : "user");
                    } else {
                        callback.onRoleResult("user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user role", e);
                    callback.onRoleResult("user");
                });
    }

    public void getUserData(String uid, OnUserDataCallback callback) {
        mFirestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onUserDataResult(documentSnapshot);
                    } else {
                        callback.onUserDataResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onUserDataResult(null);
                });
    }

    // ===================== APK MANAGEMENT =====================

    public CollectionReference getDownloadsCollection() {
        return mFirestore.collection("downloads");
    }

    public void addApk(ApkModel apk, OnSuccessListener<DocumentReference> onSuccess,
                       OnFailureListener onFailure) {
        Map<String, Object> apkData = new HashMap<>();
        apkData.put("title", apk.getTitle());
        apkData.put("version", apk.getVersion());
        apkData.put("size", apk.getSize());
        apkData.put("description", apk.getDescription());
        apkData.put("imageUrl", apk.getImageUrl());
        apkData.put("downloadUrl", apk.getDownloadUrl());
        apkData.put("category", apk.getCategory());
        apkData.put("dateCreated", apk.getDateCreated());

        mFirestore.collection("downloads").add(apkData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void updateApk(String id, ApkModel apk,
                          OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        Map<String, Object> apkData = new HashMap<>();
        apkData.put("title", apk.getTitle());
        apkData.put("version", apk.getVersion());
        apkData.put("size", apk.getSize());
        apkData.put("description", apk.getDescription());
        apkData.put("imageUrl", apk.getImageUrl());
        apkData.put("downloadUrl", apk.getDownloadUrl());
        apkData.put("category", apk.getCategory());

        mFirestore.collection("downloads").document(id)
                .update(apkData)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteApk(String id, OnSuccessListener<Void> onSuccess,
                          OnFailureListener onFailure) {
        mFirestore.collection("downloads").document(id)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void getApkDetail(String id, OnApkDetailCallback callback) {
        mFirestore.collection("downloads").document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ApkModel apk = documentSnapshot.toObject(ApkModel.class);
                        if (apk != null) {
                            apk.setId(documentSnapshot.getId());
                        }
                        callback.onApkDetailResult(apk);
                    } else {
                        callback.onApkDetailResult(null);
                    }
                })
                .addOnFailureListener(e -> callback.onApkDetailResult(null));
    }

    // ===================== INTERFACES =====================

    public interface OnRoleCallback {
        void onRoleResult(String role);
    }

    public interface OnUserDataCallback {
        void onUserDataResult(DocumentSnapshot document);
    }

    public interface OnApkDetailCallback {
        void onApkDetailResult(ApkModel apk);
    }
}
