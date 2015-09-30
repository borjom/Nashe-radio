package com.randomname.vlad.nasheradio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.randomname.vlad.nasheradio.activitys.SettingsActivity;
import com.randomname.vlad.nasheradio.fragments.MainFragment;
import com.randomname.vlad.nasheradio.services.MusicService;
import com.randomname.vlad.nasheradio.util.Constants;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentCallbacks {

    Toolbar toolbar;

    MusicService mService;
    boolean mBound = false;

    MainFragment musicFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.nashe_label);

        setSupportActionBar(toolbar);

        initSharedPreferences();

        Intent startIntent = new Intent(MainActivity.this, MusicService.class);
        startService(startIntent);

        musicFragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.music_fragment);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!mService.getIsPlaying() && !mService.getIsPrepared()) {
            Intent stopIntent = new Intent(MainActivity.this, MusicService.class);
            stopService(stopIntent);
        }

        mService.isAttached = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return false;
    }

    @Override
    public void onPlayStopBtnClick() {
        if (mBound) {
            mService.togglePlaying();
        }

    }

    @Override
    public void onStationChanged() {
        if (mBound) {
            mService.restartPlayer();
        }
    }

    @Override
    public Boolean getPreparationState() {
        if (MusicService.inPreparedState != null) {
            return MusicService.inPreparedState;
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            musicFragment.setIsPlaying(mService.getIsPlaying());

            mService.isAttached = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void initSharedPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        if (!prefs.contains(Constants.SHARED_PREFERENCES.CURRENT_CHANNEL)) {
            String[] urisArray = Constants.STATIONS.STATIONS[0];

            prefs.edit().putString(
                    Constants.SHARED_PREFERENCES.CURRENT_CHANNEL,
                    urisArray[1]
                    ).apply();
        }
    }

}
