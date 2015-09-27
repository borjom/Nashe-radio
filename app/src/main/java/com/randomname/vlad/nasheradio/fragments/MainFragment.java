package com.randomname.vlad.nasheradio.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.melnykov.fab.FloatingActionButton;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainFragment extends Fragment {

    RestAdapter restAdapter;
    NasheApi nasheApi;


    @Bind(R.id.btn_play) FloatingActionButton playStopBtn;
    @Bind(R.id.img_album_art) ImageView albumArt;
    @Bind(R.id.text_song_name) TextView textSong;
    @Bind(R.id.text_song_author) TextView textArtist;
    @Bind(R.id.text_bitrate_status) TextView bitrateStatus;
    @Bind(R.id.switch_quality) SwitchButton switchButton;
    @Bind(R.id.progress_bar) SmoothProgressBar progressBar;

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

        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        Boolean status = prefs.getBoolean(Constants.SHARED_PREFERENCES.QUALITY_STATUS, true);
        switchButton.setChecked(status);

        if (status) {
            bitrateStatus.setText(R.string.high_quality);
        } else {
            bitrateStatus.setText(R.string.low_quality);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION.MUSIC_EVENT));

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mNewStatusReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION.NEW_STATUS_EVENT));

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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mNewStatusReceiver);
    }

    @OnClick (R.id.btn_play)
    public void playStopBtnClick() {
        mainFragmentCallbacks.onPlayStopBtnClick();
    }

    public interface MainFragmentCallbacks {
        public void onPlayStopBtnClick();
        public void onStationChanged();
    }

    @OnCheckedChanged (R.id.switch_quality)
    public void qualityChangedListener(SwitchButton button) {
        String[] urisArray = Constants.STATIONS.STATIONS[0];
        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);
        String uri;
        Boolean qualityStatus;
        if (button.isChecked()) {
            bitrateStatus.setText(R.string.high_quality);
            uri = urisArray[1];
            qualityStatus = true;
        } else {
            bitrateStatus.setText(R.string.low_quality);
            uri = urisArray[0];
            qualityStatus = false;
        }

        prefs.edit().putString(
                Constants.SHARED_PREFERENCES.CURRENT_CHANNEL,
                uri
        ).putBoolean(Constants.SHARED_PREFERENCES.QUALITY_STATUS,
                qualityStatus).apply();

        mainFragmentCallbacks.onStationChanged();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.START_MUSIC)) {
                playStopBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
                progressBar.progressiveStop();
            } else if(intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.STOP_MUSIC)) {
                playStopBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
            }

            if (intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.MUSIC_ERROR)) {
                progressBar.progressiveStop();
                progressBar.setVisibility(View.INVISIBLE);
            } else if(intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.MUSIC_PREPARE)) {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.progressiveStart();
            }
        }
    };

    private BroadcastReceiver mNewStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NasheModel model = intent.getParcelableExtra(Constants.BROADCAST_ACTION.MESSAGE);

            updatePlayerInfo(model);
        }
    };

    public void setIsPlaying(Boolean isPlaying) {
        if (isPlaying) {
            playStopBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
        } else {
            playStopBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_play));
        }
    }

    public void updatePlayerInfo(NasheModel nasheModel) {
        if (textArtist.getText().equals(nasheModel.getArtist())) {
            return;
        }

        Picasso.with(getActivity())
                .load(nasheModel.getArt())
                .noPlaceholder()
                .error(R.drawable.nashe_big_white)
                .into(albumArt);

        textArtist.setText(nasheModel.getArtist());
        textSong.setText(nasheModel.getSong());
    }
}
