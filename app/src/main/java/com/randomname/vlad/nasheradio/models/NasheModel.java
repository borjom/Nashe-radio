package com.randomname.vlad.nasheradio.models;

public class NasheModel {
    private String song;

    private String[] lastTracks;

    private String art;

    private String artist;

    public String getSong ()
    {
        return song;
    }

    public void setSong (String song)
    {
        this.song = song;
    }

    public String[] getLastTracks ()
    {
        return lastTracks;
    }

    public void setLastTracks (String[] lastTracks)
    {
        this.lastTracks = lastTracks;
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
        return "NahseModel [song = "+song+", lastTracks = "+lastTracks+", art = "+art+", artist = "+artist+"]";
    }
}