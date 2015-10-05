package com.randomname.vlad.nasheradio.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.randomname.vlad.nasheradio.fragments.StationFragment;

import java.util.ArrayList;
import java.util.List;

public class StationsAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> fragments;


    public StationsAdapter(FragmentManager fm) {

        super(fm);

        this.fragments = new ArrayList<Fragment>();
        fragments.add(new StationFragment());
        fragments.add(new StationFragment());
    }

    @Override

    public Fragment getItem(int position) {

        return this.fragments.get(position);

    }


    @Override

    public int getCount() {

        return this.fragments.size();

    }

}

