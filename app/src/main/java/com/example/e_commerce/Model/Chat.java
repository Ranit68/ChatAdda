package com.example.e_commerce.Model;

public class Chat {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean isSeen;
    private String mediaUrl;
    private String messageType;
    private boolean isUploaded;
    private boolean isRecived;

    // Empty constructor required for Firebase
    public Chat() {}

    // Constructor for text messages
    public Chat(String senderId, String receiverId, String message, long timestamp, boolean isSeen, boolean isRecived) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.isRecived = isRecived;
        this.mediaUrl = ""; // Default empty
        this.messageType = "text"; // Default to text
    }

    // Constructor for media messages
    public Chat(String senderId, String receiverId, String mediaUrl, long timestamp, boolean isSeen, String messageType, boolean isUploaded) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.mediaUrl = mediaUrl;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.messageType = messageType;
        this.message = ""; // No text message for media
        this.isUploaded = isUploaded;
    }

    // Getters and Setters


    public boolean isRecived() {
        return isRecived;
    }

    public void setRecived(boolean recived) {
        isRecived = recived;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isSeen() { return isSeen; }
    public void setSeen(boolean seen) { isSeen = seen; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }
}