package com.randomname.vlad.nasheradio.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.activitys.FullSreenPhotoActivity;
import com.randomname.vlad.nasheradio.util.Constants;
import com.randomname.vlad.nasheradio.util.StringUtils;
import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.model.VKApiLink;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKApiPost;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKPostArray;

import java.util.Date;

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

        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, int i) {

        VKApiPost wallPost = wallPosts.get(i);

        Long dateLong = wallPost.date;

        String postText = wallPost.text;
        String imageURL = "";
        String linkURL = "";

        String repostText = "";
        String repostImageURL = "";
        String repostLinkURL = "";

        VKAttachments postAttachments = wallPost.attachments;

        if (!postAttachments.isEmpty()) {
            for (int ii = 0; ii < postAttachments.size(); ii++) {
                VKAttachments.VKApiAttachment attachment = postAttachments.get(ii);

                if (attachment.getType().equals(VKApiConst.PHOTO)) {

                    VKApiPhoto photo = (VKApiPhoto) attachment;

                    if (imageURL.isEmpty()) {
                        imageURL = photo.photo_2560;
                        if (imageURL.isEmpty()) {
                            imageURL = photo.photo_1280;
                        }
                        if (imageURL.isEmpty()) {
                            imageURL = photo.photo_807;
                        }
                        if (imageURL.isEmpty()) {
                            imageURL = photo.photo_604;
                        }
                    }
                } else if (attachment.getType().equals("link")) {
                    VKApiLink link = (VKApiLink) attachment;

                    if (linkURL.isEmpty()) {
                        linkURL = link.url;
                    }
                }
            }
        }

        if (dateLong > 0) {
            long millisecond = dateLong * 1000;
            String dateString= DateFormat.format("dd MMMM kk:mm", new Date(millisecond)).toString();

            customViewHolder.dateText.setText(dateString);
        }

        if (!postText.isEmpty()) {

            postText = StringUtils.removeURLFromString(postText);
            postText = StringUtils.replaceVkLink(postText);

            customViewHolder.newsText.setText(Html.fromHtml(postText));
        } else {
            customViewHolder.newsText.setText("");
        }

        if (!linkURL.isEmpty()) {
            setClickableText(customViewHolder.newsLink, linkURL);
        } else {
            customViewHolder.newsLink.setText("");
        }

        if (!imageURL.isEmpty()) {
            Picasso.with(mContext)
                    .load(imageURL)
                    .noPlaceholder()
                    .into(customViewHolder.newsImage);
        } else {
            customViewHolder.newsImage.setImageResource(android.R.color.transparent);
        }

        if (postText.isEmpty() && linkURL.isEmpty() && imageURL.isEmpty()) {
            VKList<VKApiPost> copyHistory = wallPost.copy_history;


            if (copyHistory.size() > 0) {

                for (int y = 0; y < copyHistory.size(); y++) {
                    VKApiPost repost = copyHistory.get(y);
                    VKAttachments repostAttachments = repost.attachments;

                    if (repostText.isEmpty()) {
                        repostText = repost.text;
                    }

                    if (!repostAttachments.isEmpty()) {
                        for (int ii = 0; ii < repostAttachments.size(); ii++) {
                            VKAttachments.VKApiAttachment attachment = repostAttachments.get(ii);

                            if (attachment.getType().equals("link")) {
                                VKApiLink link = (VKApiLink) attachment;

                                if (repostLinkURL.isEmpty()) {
                                    repostLinkURL = link.url;
                                }

                                if (repostImageURL.isEmpty()) {
                                    repostImageURL = link.image_src;
                                }
                            }
                        }
                    }
                }
            }

            if (!repostText.isEmpty()) {

                repostText = StringUtils.removeURLFromString(repostText);
                repostText = StringUtils.replaceVkLink(repostText);

                customViewHolder.newsText.setText(Html.fromHtml(repostText));
            } else {
                customViewHolder.newsText.setText("");
            }

            if (!repostLinkURL.isEmpty()) {
                setClickableText(customViewHolder.newsLink, repostLinkURL);
            } else {
                customViewHolder.newsLink.setText("");
            }

            if (!repostImageURL.isEmpty()) {
                Picasso.with(mContext)
                        .load(repostImageURL)
                        .noPlaceholder()
                        .into(customViewHolder.newsImage);
            } else {
                customViewHolder.newsImage.setImageResource(android.R.color.transparent);
            }
        }

        if (linkURL.isEmpty() && repostLinkURL.isEmpty()) {
            String vkPostUrl = "https://vk.com/nashe?w=wall" + wallPost.from_id + "_" + wallPost.getId();

            setClickableText(customViewHolder.newsLink, vkPostUrl);
        }

        final String finalURL = !imageURL.isEmpty() ? imageURL : repostImageURL;
        customViewHolder.newsImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, FullSreenPhotoActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA.PHOTO_EXTRA, finalURL);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != wallPosts ? wallPosts.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView newsText, dateText, newsLink;
        protected ImageView newsImage;

        public CustomViewHolder(View view) {
            super(view);
            this.newsText = (TextView) view.findViewById(R.id.news_text);
            this.dateText = (TextView) view.findViewById(R.id.news_date_text);
            this.newsLink = (TextView) view.findViewById(R.id.news_link_text);
            this.newsImage = (ImageView) view.findViewById(R.id.news_image);

            this.newsText.setClickable(true);
            this.newsText.setMovementMethod (LinkMovementMethod.getInstance());
        }
    }

    private void setClickableText(TextView view, String link) {
        view.setText(Html.fromHtml("<a href=\"" + link + "\">" + link + "</a>"));
        view.setClickable(true);
        view.setMovementMethod (LinkMovementMethod.getInstance());
    }
}
