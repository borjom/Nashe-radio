package com.randomname.vlad.nasheradio.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.adapters.LastSongsAdapter;
import com.randomname.vlad.nasheradio.adapters.NewsAdapter;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPostArray;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NewsFragment extends Fragment {

    final String NEWS_POSTS_KEY = "newsPostsKey";
    final String RECYCLER_STATE_KEY = "recyclerStateKey";
    final String REQUEST_OFFSET_KEY = "requestOffsetKey";

    @Bind(R.id.news_recycler_view)
    RecyclerView newsRecyclerView;
    LinearLayoutManager mLinearLayoutManager;

    VKPostArray newsPosts;
    NewsAdapter newsAdapter;

    private boolean loading = false;
    int requesOffset = 0;

    public NewsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.news_fragment, container, false);
        ButterKnife.bind(this, view);

        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        newsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        newsRecyclerView.setLayoutManager(mLinearLayoutManager);

        if (savedInstanceState == null) {
            newsPosts = new VKPostArray();
        } else {
            newsPosts = savedInstanceState.getParcelable(NEWS_POSTS_KEY);
            Parcelable recyclerState = savedInstanceState.getParcelable(RECYCLER_STATE_KEY);
            mLinearLayoutManager.onRestoreInstanceState(recyclerState);
            requesOffset = savedInstanceState.getInt(REQUEST_OFFSET_KEY);
        }

        newsAdapter = new NewsAdapter(getActivity(), newsPosts);
        newsRecyclerView.setAdapter(newsAdapter);

        newsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

               int visibleItemCount = mLinearLayoutManager.getChildCount();
               int totalItemCount = mLinearLayoutManager.getItemCount();
               int pastVisiblesItems = mLinearLayoutManager.findFirstVisibleItemPosition();

               if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                   if (!loading) {
                       loading = true;
                       getWallPosts();
                   }
               }
            }
        });

        getWallPosts();

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(NEWS_POSTS_KEY, newsPosts);

        Parcelable mListState = mLinearLayoutManager.onSaveInstanceState();
        outState.putParcelable(RECYCLER_STATE_KEY, mListState);

        outState.putInt(REQUEST_OFFSET_KEY, requesOffset);

        super.onSaveInstanceState(outState);
    }

    private void getWallPosts() {

        VKParameters params = new VKParameters();
        params.put("domain", "nashe");
        params.put("count", "10");
        params.put("offset", requesOffset);

        final VKRequest request = new VKRequest("wall.get", params);

        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                requesOffset += 10;
                loading = false;

                VKPostArray posts = new VKPostArray();

                try {
                    posts.parse(response.json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                newsPosts.addAll(posts);
                newsAdapter.notifyDataSetChanged();
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                super.attemptFailed(request, attemptNumber, totalAttempts);
                loading = false;
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                loading = false;
            }
        });
    }
}
