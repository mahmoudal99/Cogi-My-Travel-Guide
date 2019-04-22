package com.example.mytravelguide.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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

    public ImagePicker(Context context) {
        this.context = context;
    }

    public void openGallery(Intent data){

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(data.getData());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            getLandmark(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getLandmark(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        Log.d("VISION", firebaseVisionCloudLandmarks.get(0).getLandmark());
                    }
                });
    }

}
