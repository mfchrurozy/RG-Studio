package com.rgstudio;

import java.io.Serializable;

public class ApkModel implements Serializable {

    private String id;
    private String title;
    private String version;
    private String size;
    private String description;
    private String imageUrl;
    private String downloadUrl;
    private String category;
    private String dateCreated;

    public ApkModel() {
        // Required empty constructor for Firestore
    }

    public ApkModel(String id, String title, String version, String size,
                    String description, String imageUrl, String downloadUrl,
                    String category, String dateCreated) {
        this.id = id;
        this.title = title;
        this.version = version;
        this.size = size;
        this.description = description;
        this.imageUrl = imageUrl;
        this.downloadUrl = downloadUrl;
        this.category = category;
        this.dateCreated = dateCreated;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getVersion() { return version; }
    public String getSize() { return size; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getDownloadUrl() { return downloadUrl; }
    public String getCategory() { return category; }
    public String getDateCreated() { return dateCreated; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setVersion(String version) { this.version = version; }
    public void setSize(String size) { this.size = size; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public void setCategory(String category) { this.category = category; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
}
