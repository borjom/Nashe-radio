package com.randomname.vlad.nasheradio.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.adapters.LastSongsAdapter;
import com.randomname.vlad.nasheradio.adapters.SpacesItemDecoration;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;

import java.lang.reflect.Array;
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

    final String SONGS_ARRAY_KEY = "lastSongsArrayList";
    final String RECYCLER_STATE_KEY = "recyclerStateKey";

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

        lastSongsRecycler.setItemAnimator(new DefaultItemAnimator());
        lastSongsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        lastSongsRecycler.addItemDecoration(new SpacesItemDecoration(1));

        if (savedInstanceState == null) {
            lastSongsArray = new ArrayList<>();
        } else {
            lastSongsArray = savedInstanceState.getStringArrayList(SONGS_ARRAY_KEY);
            Parcelable recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE_KEY);
            lastSongsRecycler.getLayoutManager().onRestoreInstanceState(recyclerState);
        }

        lastSongsAdapter = new LastSongsAdapter(getActivity(), lastSongsArray);
        lastSongsRecycler.setAdapter(lastSongsAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(SONGS_ARRAY_KEY, lastSongsArray);

        Parcelable mListState = lastSongsRecycler.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_STATE_KEY, mListState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNewStatusReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION.NEW_STATUS_EVENT));

        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);
        int currentStation = prefs.getInt(Constants.SHARED_PREFERENCES.CURRENT_STATION, 0);
        String path = Constants.API.STATIONS[currentStation];

        nasheApi.getCurrentSong(path, new Callback<NasheModel>() {
            @Override
            public void success(NasheModel nasheModel, Response response) {
                if (response.getStatus() == 200) {
                    updateList(nasheModel);
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private BroadcastReceiver mNewStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NasheModel model = intent.getParcelableExtra(Constants.BROADCAST_ACTION.MESSAGE);
            updateList(model);
        }
    };

    private void updateList(NasheModel nasheModel) {
        String[] songs = nasheModel.getLastTracks();
        List<String> newArray = Arrays.asList(songs);

        if (lastSongsArray.equals(newArray)) {
            return;
        }

        lastSongsArray.clear();
        lastSongsArray.addAll(newArray);
        lastSongsAdapter.notifyItemRangeChanged(0, newArray.size());
    }
}
