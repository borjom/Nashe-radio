package com.randomname.vlad.nasheradio.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

import com.randomname.vlad.nasheradio.MainActivity;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.util.Constants;

public class MusicService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

    private final IBinder mBinder = new LocalBinder();

    private Notification notification;

    private MediaPlayer player;
    WifiManager.WifiLock wifiLock;
    AudioManager audioManager;
    private int volumeLevel = 0;


    private Boolean isPlaying = false;
    private Boolean audioFocusGranted = false;
    private Boolean inPreparedState = false;
    private Boolean isInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(Constants.LOG_TAG.SERVICE, "On create");
        audioManager = (AudioManager) getSystemService(getApplicationContext().AUDIO_SERVICE);
        initMusicPlayer();
        initNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Constants.LOG_TAG.SERVICE, "on start command");

        if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION.STOP_FOREGROUND_ACTION)) {
            stopPlaying();
            stopForeground(true);
            isInForeground = false;
        } else if (intent.getAction() != null && intent.getAction().equals(Constants.ACTION.PAUSE_PLAY_ACTION)) {
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
        Log.d(Constants.LOG_TAG.SERVICE, "on unbind");
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.LOG_TAG.SERVICE, "on destroy");
        player.release();
        super.onDestroy();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        stopForeground(true);
        isInForeground = false;
        inPreparedState = false;
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();
        isPlaying = true;
        inPreparedState = false;

        if (!isInForeground) {
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
            isInForeground = true;
        }

        Intent intent = new Intent(Constants.BROADCAST_ACTION.MUSIC_EVENT);
        intent.putExtra(Constants.BROADCAST_ACTION.MESSAGE, Constants.BROADCAST_ACTION.START_MUSIC);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
            Log.d(Constants.LOG_TAG.SERVICE, "return binder");
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
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
                .setSmallIcon(R.drawable.nashe_small)
                .setOngoing(true);

        RemoteViews m_view = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_player);
        m_view.setImageViewResource(R.id.player_pause, android.R.drawable.ic_media_pause);
        m_view.setTextViewText(R.id.song_name, "Название песни");
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
                    "http://nashe.streamr.ru/nashe-128.mp3"
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.prepareAsync();
        inPreparedState = true;
    }

    public void stopPlaying() {
        isPlaying = false;
        wifiLock.release();
        wifiLock = null;
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

    public Boolean getIsPlaying() {
        return isPlaying;
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

