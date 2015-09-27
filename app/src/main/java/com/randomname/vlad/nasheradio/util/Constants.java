package com.randomname.vlad.nasheradio.util;

public class Constants {
    public interface ACTION {
        public static String STOP_FOREGROUND_ACTION = "com.randomname.vlad.nasheradio.action.close";
        public static String PAUSE_PLAY_ACTION = "com.randomname.vlad.nasheradio.action.pause.play";
    }

    public interface BROADCAST_ACTION {
        public static String MUSIC_EVENT = "com.randomname.vlad.nasheradio.broadcast.music.event";
        public static String NEW_STATUS_EVENT = "com.randomname.vlad.nasheradio.broadcast.new.status.event";
        public static String START_MUSIC = "com.randomname.vlad.nasheradio.broadcast.start.music";
        public static String MUSIC_ERROR = "com.randomname.vlad.nasheradio.broadcast.music.error";
        public static String MUSIC_PREPARE = "com.randomname.vlad.nasheradio.broadcast.music.prepare";
        public static String STOP_MUSIC = "com.randomname.vlad.nasheradio.broadcast.stop.music";
        public static String MESSAGE = "message";
    }

    public interface SHARED_PREFERENCES {
        public static String PREF_NAME = "nasheradio.prefs";
        public static String CURRENT_CHANNEL = "current.channel";
        public static String QUALITY_STATUS = "quality.status";
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

    public interface STATIONS {
        public static String[][] STATIONS = {
                new String[] {
                    "http://nashe.streamr.ru/nashe-64.mp3",
                    "http://nashe.streamr.ru/nashe-128.mp3"
                }
        };
    }
}