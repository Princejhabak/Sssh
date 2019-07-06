package com.example.android.ssshdbpractice;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note_table")
public class Note {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;

    private String description;

    private int radius;

    private String mode;

    private double latitude;

    private double longitude;

    private String placeId;

    public Note(String title, String description, int radius, String mode, double latitude, double longitude, String placeId) {
        this.title = title;
        this.description = description;
        this.radius = radius;
        this.mode = mode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeId = placeId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getRadius() {
        return radius;
    }

    public String getMode() {
        return mode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceId() {
        return placeId;
    }
}
