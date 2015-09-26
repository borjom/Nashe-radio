package com.randomname.vlad.nasheradio.models;

import android.os.Parcel;
import android.os.Parcelable;

public class NasheModel implements Parcelable {
    private String song;

    private String art;

    private String artist;

    public NasheModel() {
    }

    public NasheModel(Parcel in) {
        song = in.readString();
        art = in.readString();
        artist = in.readString();
    }

    public String getSong ()
    {
        return song;
    }

    public void setSong (String song)
    {
        this.song = song;
    }

    public String getArt ()
    {
        return art;
    }

    public void setArt (String art)
    {
        this.art = art;
    }

    public String getArtist ()
    {
        return artist;
    }

    public void setArtist (String artist)
    {
        this.artist = artist;
    }

    @Override
    public String toString()
    {
        return "NahseModel [song = "+song+", art = "+art+", artist = "+artist+"]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(song);
        dest.writeString(art);
        dest.writeString(artist);
    }

    public static final Parcelable.Creator<NasheModel> CREATOR = new Parcelable.Creator<NasheModel>() {
        public NasheModel createFromParcel(Parcel in) {
            return new NasheModel(in);
        }

        public NasheModel[] newArray(int size) {
            return new NasheModel[size];
        }
    };
}