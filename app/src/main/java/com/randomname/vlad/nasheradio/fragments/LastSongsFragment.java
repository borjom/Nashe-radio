package com.randomname.vlad.nasheradio.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.randomname.vlad.nasheradio.R;

import butterknife.ButterKnife;

public class LastSongsFragment extends Fragment {
    public LastSongsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.last_songs_fragment, container, false);
        ButterKnife.bind(this, view);

        return view;
    }
}
