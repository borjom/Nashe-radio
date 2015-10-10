package com.randomname.vlad.nasheradio;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.randomname.vlad.nasheradio.activitys.SettingsActivity;
import com.randomname.vlad.nasheradio.fragments.LastSongsFragment;
import com.randomname.vlad.nasheradio.fragments.MainFragment;
import com.randomname.vlad.nasheradio.fragments.NewsFragment;
import com.randomname.vlad.nasheradio.services.MusicService;
import com.randomname.vlad.nasheradio.util.Constants;

public class MainActivity extends AppCompatActivity implements MainFragment.MainFragmentCallbacks {

    final String DRAWLER_SELECTION = "drawlerSelection";
    final String TITLE = "navigationTitle";

    Toolbar toolbar;
    Drawer navDrawler;

    MusicService mService;
    boolean mBound = false;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            int position = savedInstanceState.getInt(DRAWLER_SELECTION);
            String title = savedInstanceState.getString(TITLE);
            getSupportActionBar().setTitle(title);
            initDrawler(position);
        } else {
            changeTitle();
            initDrawler(0);
        }

        initSharedPreferences();

        FragmentManager fm = getSupportFragmentManager();

        if (fm.getFragments() == null) {
            Fragment fragment = fm.findFragmentByTag("MainFragmentTag");
            if (fragment == null) {
                FragmentTransaction ft = fm.beginTransaction();
                fragment = new MainFragment();
                ft.replace(R.id.main_frame,fragment,"MainFragmentTag");
                ft.commit();
            }
        }

        Intent startIntent = new Intent(MainActivity.this, MusicService.class);
        startService(startIntent);
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
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(DRAWLER_SELECTION, navDrawler.getCurrentSelectedPosition());
        outState.putString(TITLE, String.valueOf(getSupportActionBar().getTitle()));
        super.onSaveInstanceState(outState);
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
            changeTitle();
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
            mService.isAttached = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void initDrawler(int selectedPosition) {
            navDrawler = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.drawler_player),
                        new PrimaryDrawerItem().withName(R.string.drawler_last_songs),
                        new PrimaryDrawerItem().withName(R.string.drawer_nashe_news)
                )
                .build();
        navDrawler.setSelectionAtPosition(selectedPosition);
        navDrawler.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                FragmentManager fm = getSupportFragmentManager();
                Fragment fragment;

                switch (position) {
                    case 0:
                        fragment = fm.findFragmentByTag("MainFragmentTag");
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new MainFragment();
                            ft.replace(R.id.main_frame,fragment,"MainFragmentTag");
                            ft.commit();
                            changeTitle();
                            navDrawler.closeDrawer();
                        }
                        break;
                    case 1:
                        fragment = fm.findFragmentByTag("LastSongsTag");
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new LastSongsFragment();
                            ft.replace(R.id.main_frame,fragment,"LastSongsTag");
                            ft.commit();
                            getSupportActionBar().setTitle(R.string.drawler_last_songs);
                            navDrawler.closeDrawer();
                        }
                        break;
                    case 2:
                        fragment = fm.findFragmentByTag("NasheNewsTag");
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new NewsFragment();
                            ft.replace(R.id.main_frame,fragment,"NasheNewsTag");
                            ft.commit();
                            getSupportActionBar().setTitle(R.string.nashe_news_title);
                            navDrawler.closeDrawer();
                        }
                        break;
                }
                return true;
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navDrawler.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    public void initSharedPreferences() {
        prefs = this.getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        if (!prefs.contains(Constants.SHARED_PREFERENCES.CURRENT_CHANNEL)) {
            String[] urisArray = Constants.STATIONS.STATIONS[0];

            prefs.edit().putString(
                    Constants.SHARED_PREFERENCES.CURRENT_CHANNEL,
                    urisArray[1]
                    ).apply();
        }
    }

    public void changeTitle() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        int currentStation = prefs.getInt(Constants.SHARED_PREFERENCES.CURRENT_STATION, 0);

        String[] stationsArray = getResources().getStringArray(R.array.stations_names);

        String newTitle = stationsArray[currentStation];
        try {
            getSupportActionBar().setTitle(newTitle);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && navDrawler.isDrawerOpen()) {
            navDrawler.closeDrawer();
            return false;
        } else if(keyCode == KeyEvent.KEYCODE_BACK && isTaskRoot()) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            Boolean stopOnBack = sharedPref.getBoolean("pref_exitOnBack", false);

            if (stopOnBack) {
                if (mBound) {
                    mService.stopPlaying();
                }
            }

            return super.onKeyDown(keyCode, event);
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
