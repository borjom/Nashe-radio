package com.randomname.vlad.nasheradio.fragments;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.adapters.ChartAdapter;
import com.randomname.vlad.nasheradio.adapters.LastSongsAdapter;
import com.randomname.vlad.nasheradio.adapters.SpacesItemDecoration;
import com.randomname.vlad.nasheradio.api.NasheApi;
import com.randomname.vlad.nasheradio.models.ChartModel;
import com.randomname.vlad.nasheradio.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChartFragment extends Fragment {

    final String SONGS_ARRAY_KEY = "lastSongsArrayList";
    final String RECYCLER_STATE_KEY = "recyclerStateKey";

    RestAdapter restAdapter;
    NasheApi nasheApi;

    @Bind(R.id.chart_recycler_view)
    RecyclerView chartRecycler;

    ArrayList<ChartModel> chartArray;
    private ChartAdapter chartAdapter;

    public ChartFragment() {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chart_fragment, container, false);

        ButterKnife.bind(this, view);

        chartRecycler.setItemAnimator(new DefaultItemAnimator());
        chartRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        chartRecycler.addItemDecoration(new SpacesItemDecoration(1));

        if (savedInstanceState == null) {
            chartArray = new ArrayList<>();
        } else {
            chartArray = savedInstanceState.getParcelableArrayList(SONGS_ARRAY_KEY);
            Parcelable recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE_KEY);
            chartRecycler.getLayoutManager().onRestoreInstanceState(recyclerState);
        }

        chartAdapter = new ChartAdapter(getActivity(), chartArray);
        chartRecycler.setAdapter(chartAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SONGS_ARRAY_KEY, chartArray);

        Parcelable mListState = chartRecycler.getLayoutManager().onSaveInstanceState();
        outState.putParcelable(RECYCLER_STATE_KEY, mListState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (chartArray.size() == 0) {
            getChart();
        }
    }

    private void getChart() {
        nasheApi.getChart(new Callback<ChartModel[]>() {
            @Override
            public void success(ChartModel[] chartModels, Response response) {
                List<ChartModel> newArray = Arrays.asList(chartModels);

                chartArray.clear();
                chartArray.addAll(newArray);
                chartAdapter.notifyItemRangeChanged(0, newArray.size());
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getActivity(), R.string.error_connetion, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
