package com.randomname.vlad.nasheradio.fragments;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.kyleduo.switchbutton.SwitchButton;
import com.melnykov.fab.FloatingActionButton;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.adapters.CancelableViewPager;
import com.randomname.vlad.nasheradio.adapters.StationsAdapter;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnPageChange;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainFragment extends Fragment implements ViewSwitcher.ViewFactory {

    final String SONG_KEY = "song";
    final String ARTIST_KEY = "artist";

    RestAdapter restAdapter;
    NasheApi nasheApi;

    StationsAdapter stationAdapter;

    @Bind(R.id.btn_play) FloatingActionButton playStopBtn;
    @Bind(R.id.text_song_name) TextSwitcher textSong;
    @Bind(R.id.text_song_author) TextSwitcher textArtist;
    @Bind(R.id.text_bitrate_status) TextView bitrateStatus;
    @Bind(R.id.switch_quality) SwitchButton switchButton;
    @Bind(R.id.progress_bar) SmoothProgressBar progressBar;
    @Bind(R.id.pager_stations) CancelableViewPager stationsPager;

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        TextView songText = (TextView) textSong.getCurrentView();
        TextView artText = (TextView) textArtist.getCurrentView();

        outState.putString(SONG_KEY, String.valueOf(songText.getText()));
        outState.putString(ARTIST_KEY, String.valueOf(artText.getText()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);
        ButterKnife.bind(this, view);

        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);
        stationAdapter = new StationsAdapter(getChildFragmentManager());
        stationsPager.setAdapter(stationAdapter);
        stationsPager.setOffscreenPageLimit(1);

        Boolean status = prefs.getBoolean(Constants.SHARED_PREFERENCES.QUALITY_STATUS, true);
        switchButton.setChecked(status);

        int currentStation = prefs.getInt(Constants.SHARED_PREFERENCES.CURRENT_STATION, 0);

        stationsPager.setCurrentItem(currentStation);
        if (status) {
            bitrateStatus.setText(R.string.high_quality);
        } else {
            bitrateStatus.setText(R.string.low_quality);
        }

        textSong.setFactory(MainFragment.this);
        textSong.setInAnimation(getActivity(), R.anim.fade_in);
        textSong.setOutAnimation(getActivity(), R.anim.fade_out);

        textArtist.setFactory(MainFragment.this);
        textArtist.setInAnimation(getActivity(), R.anim.fade_in);
        textArtist.setOutAnimation(getActivity(), R.anim.fade_out);

        if (savedInstanceState != null) {
            textSong.setText(savedInstanceState.getString(SONG_KEY));
            textArtist.setText(savedInstanceState.getString(ARTIST_KEY));
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

        if (mainFragmentCallbacks.getPreparationState()) {
            progressBar.progressiveStart();
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.progressiveStop();
            progressBar.setVisibility(View.INVISIBLE);
        }

        String path = Constants.API.STATIONS[stationsPager.getCurrentItem()];

        nasheApi.getCurrentSong(path, new Callback<NasheModel>() {
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

    @Override
    public View makeView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        return (TextView) inflater.inflate(R.layout.text_switcher_view, null);
    }

    public interface MainFragmentCallbacks {
        public void onPlayStopBtnClick();
        public void onStationChanged();
        public Boolean getPreparationState();
    }

    @OnCheckedChanged (R.id.switch_quality)
    public void qualityChangedListener(SwitchButton button) {

        int currentStation = stationsPager.getCurrentItem();

        String[] urisArray = Constants.STATIONS.STATIONS[currentStation];
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

    @OnPageChange (R.id.pager_stations)
    public void stationChangedListener() {

        if (stationsPager.getPagingEnabled()) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            stationsPager.setPagingEnabled(true);
                        }
                    },
                    500);
        }

        stationsPager.setPagingEnabled(false);

        SharedPreferences prefs = getActivity().getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        int currentStation = stationsPager.getCurrentItem();
        Boolean currentQualityBool = prefs.getBoolean(Constants.SHARED_PREFERENCES.QUALITY_STATUS, true);
        int currentQuality = (currentQualityBool) ? 1 : 0;

        String[] urisArray = Constants.STATIONS.STATIONS[currentStation];

        String uri = urisArray[currentQuality];

        prefs.edit().putString(
                Constants.SHARED_PREFERENCES.CURRENT_CHANNEL,
                uri
        ).putInt(Constants.SHARED_PREFERENCES.CURRENT_STATION,
                currentStation).apply();

        mainFragmentCallbacks.onStationChanged();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(Constants.BROADCAST_ACTION.MESSAGE).equals(Constants.BROADCAST_ACTION.START_MUSIC)) {
                playStopBtn.setImageDrawable(ContextCompat.getDrawable(getActivity(), android.R.drawable.ic_media_pause));
                progressBar.progressiveStop();
		        progressBar.setVisibility(View.INVISIBLE);
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

        StationFragment currentFragment = (StationFragment) stationAdapter.instantiateItem(stationsPager, stationsPager.getCurrentItem());

        currentFragment.setNewCover(nasheModel.getArt());

        textArtist.setText(nasheModel.getArtist());
        textSong.setText(nasheModel.getSong());
    }
}
