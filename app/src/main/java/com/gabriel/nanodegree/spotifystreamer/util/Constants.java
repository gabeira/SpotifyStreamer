package com.gabriel.nanodegree.spotifystreamer.util;

/**
 * Created by gabriel on 6/14/15.
 */
public class Constants {

    //Artist
    public static final String ARTISTS_FRAGMENT_TAG = "artists_fragment";

    //TopTracks parameters
    public static final String TOPTRACKS_FRAGMENT_TAG = "toptracks_fragment";
    public static final String ARG_ARTIST_NAME_KEY = "artistname";
    public static final String ARG_ARTIST_ID_KEY = "artistid";

    //Player parameters
    public static final String PLAYER_FRAGMENT_TAG = "player_fragment";
    public static final int PLAYER_LOADING = 1;
    public static final int PLAYER_READYTOPLAY = 0;
    public static final int PLAYER_FINISHED = 2;
    public static final int PLAYER_PAUSED = 3;
    public static final String BROADCAST_STATUS = "broadcast_status";

}
