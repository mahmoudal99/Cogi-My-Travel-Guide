package com.cogi.mytravelguide.models;


import com.google.android.libraries.places.api.model.PhotoMetadata;

public class LandmarkSwipeModel {

    private PhotoMetadata image;

    public LandmarkSwipeModel(PhotoMetadata image) {
        this.image = image;
    }

    public PhotoMetadata getImage() {
        return image;
    }

    public void setImage(PhotoMetadata image) {
        this.image = image;
    }

}
