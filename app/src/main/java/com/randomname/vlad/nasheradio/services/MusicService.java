package com.randomname.vlad.nasheradio.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.randomname.vlad.nasheradio.MainActivity;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.NasheModel;
import com.randomname.vlad.nasheradio.util.Constants;

import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MusicService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

    private final IBinder mBinder = new LocalBinder();

    private Notification notification;

    private MediaPlayer player;
    private WifiManager.WifiLock wifiLock;
    private AudioManager audioManager;
    private PowerManager powerManager;

    private BroadcastReceiver powerReceiver;

    private Timer updateTimer;

    private static Boolean isPlaying = false;
    private Boolean audioFocusGranted = false;
    public static Boolean inPreparedState;
    private Boolean isInForeground = false;
    private Boolean restartAfterPreparation = false;

    public Boolean isAttached = false;

    RestAdapter restAdapter;
    NasheApi nasheApi;

    @Override
    public void onCreate() {
        super.onCreate();

        if (inPreparedState == null) {
            inPreparedState = false;
        }

        audioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
        initMusicPlayer();

        initNotification();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(Constants.API.BASE_URL)
                .build();

        nasheApi = restAdapter.create(NasheApi.class);
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        updateTimer = new Timer();
        startUpdating();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);

        if (powerReceiver == null) {
            powerReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                        getCurrentSong();
                    }
                }
            };
        }

        registerReceiver(powerReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null && intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            stopPlaying();
            stopForeground(true);
            isInForeground = false;
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)) {
            toggleNotificationPausePlay();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        if (!isPlaying || !isInForeground) {
            player.release();
            stopUpdating();

            if (powerReceiver != null) {
                unregisterReceiver(powerReceiver);
            }
        }

        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        inPreparedState = false;

        startPlaying();
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        isPlaying = true;
        inPreparedState = false;

        if (restartAfterPreparation) {
            restartAfterPreparation = false;
            stopPlaying();
            startPlaying();
            return;
        }

        if (!isInForeground) {
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
            isInForeground = true;
        }

        if (isPlaying) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                notification.contentView.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_pause);
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                notification.contentView.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_play);
            }
        }

        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        mNotificationManager.notify(
                Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);

        Intent intent = new Intent(Constants.BROADCAST_ACTION.MUSIC_EVENT);
        intent.putExtra(Constants.BROADCAST_ACTION.MESSAGE, Constants.BROADCAST_ACTION.START_MUSIC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        getCurrentSong();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            player.stop();
            isPlaying = false;
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
           startPlaying();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            audioManager.abandonAudioFocus(this);
        }

    }

    public class LocalBinder extends Binder {
        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    private void updateNotification(NasheModel nasheModel) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            notification.contentView.setTextViewText(R.id.song_name, nasheModel.getSong());
        }

        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        mNotificationManager.notify(
                Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

    private void startUpdating() {
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isInForeground || isAttached) {
                    boolean screenOn;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                        screenOn = powerManager.isInteractive();
                    } else {
                        screenOn = powerManager.isScreenOn();
                    }

                    if (screenOn) {
                        getCurrentSong();
                    }
                }

            }
        }, 0, 30000);
    }

    private void stopUpdating() {
        updateTimer.cancel();
        updateTimer.purge();
    }

    private void getCurrentSong() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);
        int currentStation = prefs.getInt(Constants.SHARED_PREFERENCES.CURRENT_STATION, 0);
        String path = Constants.API.STATIONS[currentStation];

        nasheApi.getCurrentSong(path, new Callback<NasheModel>() {
            @Override
            public void success(NasheModel nasheModel, Response response) {
                if (response.getStatus() == 200) {
                    if (isPlaying || isInForeground) {
                        updateNotification(nasheModel);
                    }
                    if (isAttached) {
                        sendNewStatus(nasheModel);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
            }
        });
    }

    private void sendNewStatus(NasheModel nasheModel) {
        Intent intent = new Intent(Constants.BROADCAST_ACTION.NEW_STATUS_EVENT);
        intent.putExtra(Constants.BROADCAST_ACTION.MESSAGE, nasheModel);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initMusicPlayer() {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnErrorListener(this);
        player.setOnPreparedListener(this);
    }

    private void initNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(Constants.ACTION.STOP_FOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);

        Intent pausePlayIntent = new Intent(this, MusicService.class);
        pausePlayIntent.setAction(Constants.ACTION.PAUSE_PLAY_ACTION);
        PendingIntent ppausePlayIntent = PendingIntent.getService(this, 0,
                pausePlayIntent, 0);

        NotificationCompat.Builder m_builder = (NotificationCompat.Builder) new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_nashe_notification)
                .setOngoing(true);

        RemoteViews m_view = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_player);

        if (isPlaying) {
            m_view.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_pause);
        } else {
            m_view.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_play);
        }

        m_view.setTextViewText(R.id.song_name, "");
        m_builder.setContent(m_view);
        m_builder.setContentIntent(pendingIntent);

        m_view.setOnClickPendingIntent(R.id.close_notification, pcloseIntent);
        m_view.setOnClickPendingIntent(R.id.player_pause, ppausePlayIntent);

        notification = m_builder.build();
    }

    public void startPlaying() {
        if (inPreparedState) {
            return;
        }

        if (wifiLock == null) {
            wifiLock = ((WifiManager) getSystemService(getApplicationContext().WIFI_SERVICE))
                    .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

            wifiLock.acquire();
        }

        if (!audioFocusGranted) {
            int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                audioFocusGranted = true;
            }
        }

        player.reset();

        SharedPreferences prefs = this.getSharedPreferences(
                Constants.SHARED_PREFERENCES.PREF_NAME, Context.MODE_PRIVATE);

        try {
            player.setDataSource(prefs.getString(
                    Constants.SHARED_PREFERENCES.CURRENT_CHANNEL,
                    "http://81.19.85.200/nashe128.mp3"
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        inPreparedState = true;

        Intent intent = new Intent(Constants.BROADCAST_ACTION.MUSIC_EVENT);
        intent.putExtra(Constants.BROADCAST_ACTION.MESSAGE, Constants.BROADCAST_ACTION.MUSIC_PREPARE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void stopPlaying() {
        isPlaying = false;
        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
        audioManager.abandonAudioFocus(this);
        audioFocusGranted = false;
        player.stop();
        Intent intent = new Intent(Constants.BROADCAST_ACTION.MUSIC_EVENT);
        intent.putExtra(Constants.BROADCAST_ACTION.MESSAGE, Constants.BROADCAST_ACTION.STOP_MUSIC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void togglePlaying() {
        if (isPlaying) {
            stopPlaying();
            stopForeground(true);
            isInForeground = false;
        } else {
            startPlaying();
        }
    }

    public void restartPlayer() {
        if (isPlaying && !inPreparedState) {
            stopPlaying();
            startPlaying();
        } else if (inPreparedState) {
            restartAfterPreparation = true;
        }

        getCurrentSong();
    }

    public static Boolean getIsPlaying() {
        return isPlaying;
    }
    public Boolean getIsPrepared() {
        return inPreparedState;
    }

    public void toggleNotificationPausePlay() {
        if (isPlaying) {
            stopPlaying();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                notification.contentView.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_play);
            }
        } else {
            startPlaying();
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                notification.contentView.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_pause);
            }
        }

        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        mNotificationManager.notify(
                Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                notification);
    }

}

