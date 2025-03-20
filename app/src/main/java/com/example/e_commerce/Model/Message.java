package com.example.e_commerce.Model;

public class Message {
    private String senderId;
    private String message;

    public Message() {}

    public Message(String senderId, String message) {
        this.senderId = senderId;
        this.message = message;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
}
