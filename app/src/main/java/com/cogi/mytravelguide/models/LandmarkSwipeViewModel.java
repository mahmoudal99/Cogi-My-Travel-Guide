package com.cogi.mytravelguide.models;


import com.google.android.libraries.places.api.model.PhotoMetadata;

public class LandmarkSwipeViewModel {

    private PhotoMetadata image;

    public LandmarkSwipeViewModel(PhotoMetadata image) {
        this.image = image;
    }

    public PhotoMetadata getImage() {
        return image;
    }

    public void setImage(PhotoMetadata image) {
        this.image = image;
    }

}
