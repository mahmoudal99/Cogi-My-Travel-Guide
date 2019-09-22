package com.example.mytravelguide.utils;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.mytravelguide.BuildConfig;
import com.example.mytravelguide.R;
import com.example.mytravelguide.TravelGuideActivity;

import java.util.List;

public class LandmarkSwipeViewAdapter extends PagerAdapter {

    private List<LandmarkSwipeModel> models;
    private LayoutInflater layoutInflater;
    private Context context;
    private ImageProcessing imageProcessing;

    public LandmarkSwipeViewAdapter(List<LandmarkSwipeModel> models, Context context) {
        this.models = models;
        this.context = context;
    }

    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.city_slideshow_item, container, false);

        ImageView imageView;

        imageView = view.findViewById(R.id.image);
        GooglePlacesApi googlePlacesApi = new GooglePlacesApi(BuildConfig.APIKEY, context);
        googlePlacesApi.setLandmarkImageWithBitmap(models.get(position).getImage(), imageView);

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
