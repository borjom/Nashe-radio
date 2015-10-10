package com.randomname.vlad.nasheradio.activitys;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.kyleduo.switchbutton.SwitchButton;
import com.randomname.vlad.nasheradio.R;
import com.randomname.vlad.nasheradio.Views.TouchImageView;
import com.randomname.vlad.nasheradio.util.Constants;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FullSreenPhotoActivity extends AppCompatActivity {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.full_screen_photo)
    TouchImageView photoImageView;

    private boolean toolbarShown = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_sreen_photo);

        ButterKnife.bind(this);

        toolbarInit();

        String imageURL = getIntent().getStringExtra(Constants.INTENT_EXTRA.PHOTO_EXTRA);

        if (!imageURL.isEmpty()) {
            Picasso.with(this)
                    .load(imageURL)
                    .noPlaceholder()
                    .into(photoImageView);
        } else {
            finish();
        }
    }

    @OnClick (R.id.full_screen_photo)
    public void photoImageClick() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            return;
        }

        if (toolbarShown) {
            toolbarShown = false;
            toolbar.animate().translationY(-toolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        } else {
            toolbarShown = true;
            toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        }
    }

    private void toolbarInit() {
        toolbar.setTitle("");

        if (android.os.Build.VERSION.SDK_INT >= 10) {
            toolbar.getBackground().setAlpha(100);
        }

        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
