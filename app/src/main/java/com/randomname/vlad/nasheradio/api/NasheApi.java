package com.randomname.vlad.nasheradio.api;

import com.randomname.vlad.nasheradio.models.NasheModel;

import retrofit.Callback;
import retrofit.http.GET;

public interface NasheApi {
    @GET("/test.php")
    public void getCurrentSong(Callback<NasheModel> response);
}

