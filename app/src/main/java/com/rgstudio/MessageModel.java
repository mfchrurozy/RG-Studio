package com.rgstudio;

public class MessageModel {

    private String messageId;
    private String senderId;
    private String senderName;
    private String message;
    private long timestamp;
    private String type; // "text", "system"

    public MessageModel() {
        // Required for Firebase
    }

    public MessageModel(String messageId, String senderId, String senderName,
                        String message, long timestamp, String type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters
    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getType() { return type; }

    // Setters
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setType(String type) { this.type = type; }
}
