package com.cogi.mytravelguide.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.cogi.mytravelguide.R;
import com.cogi.mytravelguide.utils.ImageProcessing;
import com.cogi.mytravelguide.models.CityImageModel;

public class SwipeViewAdapter extends PagerAdapter {

    private List<CityImageModel> cityImageModels;
    private Context context;

    public SwipeViewAdapter(List<CityImageModel> cityImageModels, Context context) {
        this.cityImageModels = cityImageModels;
        this.context = context;
    }

    @Override
    public int getCount() {
        return cityImageModels.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.city_slideshow_item, container, false);

        ImageView imageView;
        imageView = view.findViewById(R.id.image);
        ImageProcessing imageProcessing = new ImageProcessing(context);
        imageProcessing.new SetLandmarkImage(imageView).execute(cityImageModels.get(position).getImage());

        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
