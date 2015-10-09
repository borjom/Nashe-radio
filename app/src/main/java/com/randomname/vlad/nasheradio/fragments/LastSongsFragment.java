package com.randomname.vlad.nasheradio.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.adapters.LastSongsAdapter;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LastSongsFragment extends Fragment {

    RestAdapter restAdapter;
    NasheApi nasheApi;

    @Bind(R.id.last_songs_recycler)
    RecyclerView lastSongsRecycler;

    ArrayList<String> lastSongsArray;
    private LastSongsAdapter lastSongsAdapter;

    public LastSongsFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API.BASE_URL)
                .build();

        nasheApi = restAdapter.create(NasheApi.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.last_songs_fragment, container, false);
        ButterKnife.bind(this, view);

        lastSongsArray = new ArrayList<>();

        lastSongsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

        lastSongsAdapter = new LastSongsAdapter(getActivity(), lastSongsArray);
        lastSongsRecycler.setAdapter(lastSongsAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);
        int currentStation = prefs.getInt(Constants.SHARED_PREFERENCES.CURRENT_STATION, 0);
        String path = Constants.API.STATIONS[currentStation];

        nasheApi.getCurrentSong(path, new Callback<NasheModel>() {
            @Override
            public void success(NasheModel nasheModel, Response response) {
                if (response.getStatus() == 200) {
                    String[] songs = nasheModel.getLastTracks();
                    List<String> newArray = Arrays.asList(songs);

                    lastSongsArray.clear();
                    lastSongsArray.addAll(newArray);
                    lastSongsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }
}
