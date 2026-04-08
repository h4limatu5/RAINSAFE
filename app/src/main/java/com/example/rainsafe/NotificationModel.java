package com.example.rainsafe;

public class NotificationModel {
    private int id;
    private String title;
    private String message;
    private String timestamp;
    private String type; // e.g., "auto", "manual", "system"
    private String icon; // e.g., "rain", "in", "out", "sensor"

    public NotificationModel(int id, String title, String message, String timestamp, String type, String icon) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
        this.icon = icon;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public String getIcon() { return icon; }
}