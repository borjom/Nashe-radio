package com.randomname.vlad.nasheradio.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomname.vlad.nasheradio.R;

import java.util.ArrayList;

public class LastSongsAdapter extends RecyclerView.Adapter<LastSongsAdapter.CustomViewHolder> {
    private ArrayList<String> lastSongsArray;
    private Context mContext;

    public LastSongsAdapter(Context context, ArrayList<String> lastSongsArray) {
        this.lastSongsArray = lastSongsArray;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.last_songs_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        String song = lastSongsArray.get(i);


        customViewHolder.textView.setText(song);
    }

    @Override
    public int getItemCount() {
        return (null != lastSongsArray ? lastSongsArray.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.textView = (TextView) view.findViewById(R.id.title);
        }
    }
}
