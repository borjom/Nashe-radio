package com.randomname.vlad.nasheradio.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainFragment extends Fragment {

    RestAdapter restAdapter;
    NasheApi nasheApi;


    @Bind(R.id.btn_play) Button playStopBtn;
    @Bind(R.id.img_album_art) ImageView albumArt;
    @Bind(R.id.text_song_name) TextView textSong;
    @Bind(R.id.text_song_author) TextView textArtist;

    MainFragmentCallbacks mainFragmentCallbacks;

    public MainFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mainFragmentCallbacks = (MainFragmentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MainFragmentCallbacks");
        }
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
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION.MUSIC_EVENT));

        nasheApi.getCurrentSong(new Callback<NasheModel>() {
            @Override
            public void success(NasheModel nasheModel, Response response) {
                if (response.getStatus() == 200) {
                    updatePlayerInfo(nasheModel);
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @OnClick (R.id.btn_play)
    public void playStopBtnClick() {
        mainFragmentCallbacks.onPlayStopBtnClick();
    }

    public interface MainFragmentCallbacks {
        public void onPlayStopBtnClick();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.START_MUSIC)) {
                playStopBtn.setText("Стоп");
            } else if(intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.STOP_MUSIC)) {
                playStopBtn.setText("Играть");
            }
        }
    };

    public void setIsPlaying(Boolean isPlaying) {
        if (isPlaying) {
            playStopBtn.setText("Стоп");
        } else {
            playStopBtn.setText("Играть");
        }
    }

    public void updatePlayerInfo(NasheModel nasheModel) {
        Picasso.with(getActivity())
                .load(nasheModel.getArt())
                .noPlaceholder()
                .error(R.drawable.nashe_big_transparent)
                .into(albumArt);

        textArtist.setText(nasheModel.getArtist());
        textSong.setText(nasheModel.getSong());
    }
}
