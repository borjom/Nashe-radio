package com.randomname.vlad.nasheradio.util;

public class Constants {
    public interface ACTION {
        public static String STOP_FOREGROUND_ACTION = "com.randomname.vlad.nasheradio.action.close";
        public static String PAUSE_PLAY_ACTION = "com.randomname.vlad.nasheradio.action.pause.play";
    }

    public interface BROADCAST_ACTION {
        public static String MUSIC_EVENT = "com.randomname.vlad.nasheradio.broadcast.music.event";
        public static String START_MUSIC = "com.randomname.vlad.nasheradio.broadcast.start.music";
        public static String STOP_MUSIC = "com.randomname.vlad.nasheradio.broadcast.stop.music";
        public static String MESSAGE = "message";
    }

    public interface SHARED_PREFERENCES {
        public static String PREF_NAME = "nasheradio.prefs";
        public static String CURRENT_CHANNEL = "current.channel";
    }

    public interface LOG_TAG {
        public static String SERVICE = "MusicServiceTag";
    }

    public interface NOTIFICATION_ID {
        public static int FOREGROUND_SERVICE = 101;
    }

    public interface API {
        public static String BASE_URL = "http://pooltools.ru/api";
    }
}