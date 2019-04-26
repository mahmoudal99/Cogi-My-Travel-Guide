package com.example.mytravelguide.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

import com.example.mytravelguide.TravelGuideActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ImagePicker {

    Context context;
    TextView attractionNameTextView;

    public ImagePicker(Context context) {
        this.context = context;

    }

    public void setTextView(TextView attractionTextView){
        this.attractionNameTextView = attractionTextView;
    }

}
