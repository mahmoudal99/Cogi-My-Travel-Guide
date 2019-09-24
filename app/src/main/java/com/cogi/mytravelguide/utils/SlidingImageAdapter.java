package com.cogi.mytravelguide.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.cogi.mytravelguide.HomePageActivity;
import com.cogi.mytravelguide.R;
import com.cogi.mytravelguide.attractions.CitiesActivity;
import com.cogi.mytravelguide.models.ImageModel;
import com.cogi.mytravelguide.settings.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by Parsania Hardik on 23/04/2016.
 */
public class SlidingImageAdapter extends PagerAdapter {


    private ArrayList<ImageModel> imageModelArrayList;
    private LayoutInflater inflater;
    private Context context;
    private Button exploreButton;
    private ImageProcessing imageProcessing;


    public SlidingImageAdapter(Context context, ArrayList<ImageModel> imageModelArrayList) {
        this.context = context;
        this.imageModelArrayList = imageModelArrayList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageModelArrayList.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup view, int position) {
        View imageLayout = inflater.inflate(R.layout.sliding_image_item, view, false);

        assert imageLayout != null;
        final ImageView imageView = imageLayout.findViewById(R.id.image);
        Button exploreButton = imageLayout.findViewById(R.id.exploreButton);
        final ImageView settingsImage = imageLayout.findViewById(R.id.settings);
        TextView landmarkName = imageLayout.findViewById(R.id.landmarkName);
        settingsImage.setOnClickListener(v -> context.startActivity(new Intent(context, SettingsActivity.class)));
        imageProcessing = new ImageProcessing(context);
        imageProcessing.new SetLandmarkImage(imageView).execute(imageModelArrayList.get(position).getImage_drawable());
        landmarkName.setText(imageModelArrayList.get(position).getImage_name());

        exploreButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CitiesActivity.class);
            intent.putExtra("cityName", imageModelArrayList.get(position).getImage_name());
            context.startActivity(intent);
            Toast.makeText(context, imageModelArrayList.get(position).getImage_name().toString(), Toast.LENGTH_SHORT).show();
        });

        view.addView(imageLayout, 0);

        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

}
