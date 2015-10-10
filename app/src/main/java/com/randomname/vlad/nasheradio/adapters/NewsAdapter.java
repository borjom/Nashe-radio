package com.randomname.vlad.nasheradio.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.util.StringUtils;
import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.model.VKApiLink;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPostArray;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.CustomViewHolder> {
    private VKPostArray wallPosts;
    private Context mContext;

    public NewsAdapter(Context context, VKPostArray wallPosts) {
        this.wallPosts = wallPosts;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.news_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {

        VKApiPost wallPost = wallPosts.get(i);

        String postText = wallPost.text;
        String imageURL = "";
        String linkURL = "";

        VKAttachments postAttachments = wallPost.attachments;

        if (!postAttachments.isEmpty()) {
            for (int ii = 0; ii < postAttachments.size(); ii++) {
                VKAttachments.VKApiAttachment attachment = postAttachments.get(ii);

                if (attachment.getType().equals(VKApiConst.PHOTO)) {

                    VKApiPhoto photo = (VKApiPhoto) attachment;

                    if (imageURL.isEmpty()) {
                        imageURL = photo.photo_604;
                    }
                } else if (attachment.getType().equals("link")) {
                    VKApiLink link = (VKApiLink) attachment;

                    if (linkURL.isEmpty()) {
                        linkURL = link.url;
                    }
                }
            }
        }

        if (!postText.isEmpty()) {
            customViewHolder.newsText.setText(StringUtils.removeURLFromString(postText));
        } else {
            customViewHolder.newsText.setText("empty");
        }

        if (!linkURL.isEmpty()) {
            customViewHolder.newsLink.setText(linkURL);
        } else {
            customViewHolder.newsLink.setText("empty");
        }

        if (!imageURL.isEmpty()) {
            Picasso.with(mContext)
                    .load(imageURL)
                    .noPlaceholder()
                    .into(customViewHolder.newsImage);
        } else {
            customViewHolder.newsImage.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return (null != wallPosts ? wallPosts.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView newsText;
        protected TextView newsLink;
        protected ImageView newsImage;

        public CustomViewHolder(View view) {
            super(view);
            this.newsText = (TextView) view.findViewById(R.id.news_text);
            this.newsLink = (TextView) view.findViewById(R.id.news_link_text);
            this.newsImage = (ImageView) view.findViewById(R.id.news_image);
        }
    }
}
