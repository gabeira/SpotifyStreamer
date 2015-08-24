package com.gabriel.nanodegree.spotifystreamer;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by gabriel on 6/12/15.
 */
public class SpotifyStreamerApp extends Application {

    public List<Track> trackList;
    public List<Artist> artistList;
    public boolean mIsLargeLayout;
    private int trackSelected;

    @Override
    public void onCreate() {
        super.onCreate();
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);
        trackList = new ArrayList<Track>(0);
        artistList = new ArrayList<Artist>(0);
        trackSelected = 0;
    }

    public Track getCurrentTrack() {
        if (trackList.size() > 0 && trackSelected < trackList.size())
            return trackList.get(trackSelected);
        else
            return null;
    }

    public void setTrackSelectedPosition(int _trackSelected) {
        trackSelected = _trackSelected;
    }
    public int getTrackSelectedPosition() {
        return trackSelected;
    }

    /**
     * Check for internet connection
     */
    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
