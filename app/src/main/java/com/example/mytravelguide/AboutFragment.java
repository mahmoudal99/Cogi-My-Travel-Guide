package com.example.mytravelguide;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravelguide.models.AttractionObject;

import java.util.ArrayList;

/**
 * Created by tutlane on 09-01-2018.
 */

public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState){
        return inflater.inflate(R.layout.aboutlayout, viewGroup, false);
    }
}
