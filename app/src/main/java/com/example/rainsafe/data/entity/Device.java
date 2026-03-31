package com.example.rainsafe.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "devices")
public class Device {
    @PrimaryKey
    private int id = 1; // Kita asumsikan hanya ada 1 perangkat utama
    
    private String name = "RainSafe Laundry";
    private String status = "Online";
    private String location = "Rumah";
    private String firmware = "v2.1.0";
    
    // Status Real-time
    private float temperature = 27.0f;
    private int humidity = 65;
    private float windSpeed = 12.0f;
    private int rainProbability = 5;
    private int drynessPercentage = 45;
    private boolean isClosed = false;
    
    // Settings / Otomatisasi
    private boolean isAutomationActive = true;
    private String rainSensitivity = "Medium";
    private int responseDelay = 5;
    private boolean nightMode = false;
    
    // Sensor Health
    private boolean rainSensorActive = true;
    private boolean lightSensorActive = true;
    private boolean humiditySensorActive = true;

    // Default Constructor
    public Device() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getFirmware() { return firmware; }
    public void setFirmware(String firmware) { this.firmware = firmware; }
    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }
    public int getHumidity() { return humidity; }
    public void setHumidity(int humidity) { this.humidity = humidity; }
    public float getWindSpeed() { return windSpeed; }
    public void setWindSpeed(float windSpeed) { this.windSpeed = windSpeed; }
    public int getRainProbability() { return rainProbability; }
    public void setRainProbability(int rainProbability) { this.rainProbability = rainProbability; }
    public int getDrynessPercentage() { return drynessPercentage; }
    public void setDrynessPercentage(int drynessPercentage) { this.drynessPercentage = drynessPercentage; }
    public boolean isClosed() { return isClosed; }
    public void setClosed(boolean closed) { isClosed = closed; }
    public boolean isAutomationActive() { return isAutomationActive; }
    public void setAutomationActive(boolean automationActive) { isAutomationActive = automationActive; }
    public String getRainSensitivity() { return rainSensitivity; }
    public void setRainSensitivity(String rainSensitivity) { this.rainSensitivity = rainSensitivity; }
    public int getResponseDelay() { return responseDelay; }
    public void setResponseDelay(int responseDelay) { this.responseDelay = responseDelay; }
    public boolean isNightMode() { return nightMode; }
    public void setNightMode(boolean nightMode) { this.nightMode = nightMode; }
    public boolean isRainSensorActive() { return rainSensorActive; }
    public void setRainSensorActive(boolean rainSensorActive) { this.rainSensorActive = rainSensorActive; }
    public boolean isLightSensorActive() { return lightSensorActive; }
    public void setLightSensorActive(boolean lightSensorActive) { this.lightSensorActive = lightSensorActive; }
    public boolean isHumiditySensorActive() { return humiditySensorActive; }
    public void setHumiditySensorActive(boolean humiditySensorActive) { this.humiditySensorActive = humiditySensorActive; }
}