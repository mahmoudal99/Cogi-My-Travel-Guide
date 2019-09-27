package com.cogi.mytravelguide.adapters;

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

import com.cogi.mytravelguide.R;
import com.cogi.mytravelguide.CitiesActivity;
import com.cogi.mytravelguide.models.HomePageImageModel;
import com.cogi.mytravelguide.SettingsActivity;
import com.cogi.mytravelguide.utils.ImageProcessing;

import java.util.ArrayList;

public class SlidingImageAdapter extends PagerAdapter {

    private ArrayList<HomePageImageModel> imageModelArrayList;
    private LayoutInflater inflater;
    private Context context;


    public SlidingImageAdapter(Context context, ArrayList<HomePageImageModel> imageModelArrayList) {
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

        final ImageView settingsImage = imageLayout.findViewById(R.id.settings);
        TextView landmarkName = imageLayout.findViewById(R.id.landmarkName);
        landmarkName.setText(imageModelArrayList.get(position).getImage_name());

        ImageProcessing imageProcessing = new ImageProcessing(context);
        imageProcessing.new SetLandmarkImage(imageView).execute(imageModelArrayList.get(position).getImage_drawable());

        Button exploreButton = imageLayout.findViewById(R.id.exploreButton);
        exploreButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, CitiesActivity.class);
            intent.putExtra("cityName", imageModelArrayList.get(position).getImage_name());
            context.startActivity(intent);
            Toast.makeText(context, imageModelArrayList.get(position).getImage_name(), Toast.LENGTH_SHORT).show();
        });

        settingsImage.setOnClickListener(v -> context.startActivity(new Intent(context, SettingsActivity.class)));
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
