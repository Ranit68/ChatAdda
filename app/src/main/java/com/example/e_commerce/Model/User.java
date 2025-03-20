package com.example.e_commerce.Model;

import java.io.Serializable;

public class User{
    private String userId;
    private String username;
    private String email;
    private String imgUrl;
    private String about;
    private long lastMessageTime;
    private int unseenCount;
    private String password;
    private String lastMessage;

    // Empty constructor required for Firestore
    public User() {}

    public User(String userId, String username, String email, String imgUrl, String password, String lastMessage, String about) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.imgUrl = imgUrl;
        this.about = "";
        this.password = password;
        this.lastMessage = "";
        this.lastMessageTime = 0;
        this.unseenCount = 0;
    }


    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnseenCount() {
        return unseenCount;
    }

    public void setUnseenCount(int unseenCount) {
        this.unseenCount = unseenCount;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
