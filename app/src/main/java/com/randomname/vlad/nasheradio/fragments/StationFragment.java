package com.randomname.vlad.nasheradio.fragments;


import android.content.res.Configuration;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
        try {
            Picasso.with(getActivity())
                    .load(source)
                    .noPlaceholder()
                    .error(R.drawable.nashe_big_white)
                    .into(albumArt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
