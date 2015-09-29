package com.randomname.vlad.nasheradio.fragments;


import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.randomname.vlad.nasheradio.R;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StationFragment extends Fragment {

    @Bind(R.id.image_album_art) ImageView albumArt;

    public StationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,

                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.station_fragment, container, false);

        ButterKnife.bind(this, v);

        return v;

    }

    public void setNewCover(String source) {

        String newSource = setNewQualitySource(source);

        if (newSource.equals("Default Picture")) {
            albumArt.setImageResource(R.drawable.nashe_big_white);
            return;
        }

        try {
            Picasso.with(getActivity())
                    .load(newSource)
                    .noPlaceholder()
                    .error(R.drawable.nashe_big_white)
                    .into(albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String setNewQualitySource(String source) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int qualityPrefs =Integer.parseInt(sharedPref.getString("pref_AlbumArtQuality", "4"));
        String qualityLevel;

        String[] qualityArray = {
            "none",
            "200x200bb",
            "300x300bb",
            "600x600bb",
            "800x800bb",
        };

        qualityLevel = qualityArray[qualityPrefs];

        if (qualityLevel.equals("none")) {
            return "Default Picture";
        }

        String output = source.replaceAll("1000x1000bb", qualityLevel);

        return output;
    }

}
