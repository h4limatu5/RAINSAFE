package com.example.rainsafe;

public class NotificationModel {
    private int id;
    private String title;
    private String message;
    private String timestamp;
    private String type; // e.g., "rain", "system"

    public NotificationModel(int id, String title, String message, String timestamp, String type) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
}