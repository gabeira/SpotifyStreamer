package com.gabriel.nanodegree.spotifystreamer;

import android.app.Application;

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

    @Override
    public void onCreate() {
        super.onCreate();
        trackList = new ArrayList<Track>(0);
        artistList = new ArrayList<Artist>(0);

    }
}
