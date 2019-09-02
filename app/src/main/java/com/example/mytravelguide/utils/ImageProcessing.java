package com.example.mytravelguide.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageProcessing {

    private Context context;

    public ImageProcessing(Context context) {
        this.context = context;
    }

    public void loadImageFromStorage(RelativeLayout relativeLayout) {
        String photoPath = Environment.getExternalStorageDirectory() + "/landmark.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        relativeLayout.setBackground(drawable);
    }

    public void loadImageFromStorage(ImageView imageView) {
        String photoPath = Environment.getExternalStorageDirectory() + "/city.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        imageView.setImageDrawable(drawable);
    }

    public void loadLandmarkImageFromStorage(ImageView imageView) {
        String photoPath = Environment.getExternalStorageDirectory() + "/landmark.jpg";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
        imageView.setImageDrawable(drawable);
    }

    void saveImageBitmap(Bitmap bitmap) {
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream outputStream = null;
        File file = new File(path, "image" + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try {
            outputStream = new FileOutputStream(file);

            Bitmap pictureBitmap = bitmap;
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveImageBitmap(Bitmap bitmap, String city) {
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream outputStream = null;
        File file = new File(path, city + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
        try {
            outputStream = new FileOutputStream(file);

            Bitmap pictureBitmap = bitmap;
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.flush();
            outputStream.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class SetCityImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public SetCityImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            saveImageBitmap(mIcon11, "city");
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class SetLandmarkImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public SetLandmarkImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            saveImageBitmap(mIcon11, "landmark");
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
