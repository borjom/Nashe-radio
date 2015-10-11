package com.randomname.vlad.nasheradio.models;


import android.os.Parcel;
import android.os.Parcelable;

public class ChartModel implements Parcelable {
    private String song;

    private String changeState;


    public ChartModel(Parcel in) {
        song = in.readString();
        changeState = in.readString();
    }

    public String getSong ()
    {
        return song;
    }

    public void setSong (String song)
    {
        this.song = song;
    }

    public String getChangeState ()
    {
        return changeState;
    }

    public void setChangeState (String changeState)
    {
        this.changeState = changeState;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [song = "+song+", changeState = "+changeState+"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(song);
        dest.writeString(changeState);
    }

    public static final Parcelable.Creator<ChartModel> CREATOR = new Parcelable.Creator<ChartModel>() {
        public ChartModel createFromParcel(Parcel in) {
            return new ChartModel(in);
        }

        public ChartModel[] newArray(int size) {
            return new ChartModel[size];
        }
    };
}
