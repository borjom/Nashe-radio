package com.randomname.vlad.nasheradio;

import android.app.Application;

import com.vk.sdk.VKSdk;

public class NasheApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
