package com.randomname.vlad.nasheradio.adapters;


import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.models.ChartModel;

import java.util.ArrayList;

public class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.CustomViewHolder> {
    private ArrayList<ChartModel> chartArray;
    private Context mContext;

    public ChartAdapter(Context context, ArrayList<ChartModel> chartArray) {
        this.chartArray = chartArray;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chart_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        ChartModel chartItem = chartArray.get(i);

        int stateChanges = Integer.parseInt(chartItem.getChangeState());
        String position = i + 1 < 10 ? "" + (i + 1) + "  " : "" + (i + 1);

        customViewHolder.position.setText(position);
        customViewHolder.song.setText(chartItem.getSong());

        if (stateChanges < 0) {
            customViewHolder.state.setText("" + stateChanges);
            customViewHolder.state.setTextColor(Color.parseColor("#c63032"));
            customViewHolder.stateIcon.setImageResource(R.drawable.ic_arrow_drop_down_24dp);
        } else if (stateChanges > 0) {
            customViewHolder.state.setText("  " + stateChanges);
            customViewHolder.state.setTextColor(Color.parseColor("#3da028"));
            customViewHolder.stateIcon.setImageResource(R.drawable.ic_arrow_drop_up_24dp);
        } else {
            customViewHolder.state.setText("");
            customViewHolder.stateIcon.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return (null != chartArray ? chartArray.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView position, song, state;
        protected ImageView stateIcon;

        public CustomViewHolder(View view) {
            super(view);
            this.position = (TextView) view.findViewById(R.id.position_text_view);
            this.song = (TextView) view.findViewById(R.id.song_text_view);
            this.state = (TextView) view.findViewById(R.id.state_text_view);
            this.stateIcon = (ImageView) view.findViewById(R.id.state_image_view);
        }
    }
}
