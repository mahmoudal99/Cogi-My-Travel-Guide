package com.example.mytravelguide.Models;

public class PlaceModel {

    public String placeName = "";
    public String dayVisited = "";

    public PlaceModel(String placeName, String dayVisited) {
        this.placeName = placeName;
        this.dayVisited = dayVisited;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getDayVisited() {
        return dayVisited;
    }

    public void setDayVisited(String dayVisited) {
        this.dayVisited = dayVisited;
    }
}
