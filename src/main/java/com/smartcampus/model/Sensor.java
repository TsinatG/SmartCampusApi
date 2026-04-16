/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.model;

public class Sensor {
    private String id;
    private String type;
    private String status;
    private double value;
    private int roomId; 

    // 1. Constructor
    public Sensor() {}

    public Sensor(String id, String type, String status, double value, int roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.value = value;
        this.roomId = roomId;
    }

    // 2. GETTERS
    public String getId() { 
        return id; 
    }

    public String getType() { 
        return type; 
    }

    public String getStatus() {
        return status;
    }

    public double getValue() {
        return value;
    }

    public int getRoomId() { 
        return roomId; 
    }

    // 3. SETTERS
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

} 

