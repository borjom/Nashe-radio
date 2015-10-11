package com.randomname.vlad.nasheradio.api;

import com.randomname.vlad.nasheradio.models.ChartModel;
import com.randomname.vlad.nasheradio.models.NasheModel;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

public interface NasheApi {
    @GET("/{channel}.php")
    public void getCurrentSong(@Path("channel") String channel, Callback<NasheModel> response);
    @GET("/chart.php")
    public void getChart(Callback<ChartModel[]> response);
}

