package com.gabriel.nanodegree.spotifystreamer.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;

/**
 * Created by gabriel on 6/9/15.
 */
public class SpotifyTracksTask extends AsyncTask<String,Void,List<Track>>{

    private static final String TAG = SpotifyTracksTask.class.getSimpleName();

    private Delegate _d;

    public interface Delegate {
        void onSuccessLoadTracks(List<Track> tracks);
        void onErrorLoadingTracks(Exception e);
    }

    public SpotifyTracksTask(Delegate d) {
        this._d = d;
    }

    @Override
    protected List<Track> doInBackground(final String... params) {
        try {
            if(null == params || params.length<2){
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            final Map<String, Object> options = new HashMap<String, Object>() {
                {
                    put("country",params[1].toUpperCase());
                }
            };
            Tracks results = spotify.getArtistTopTrack(params[0], options);
            Log.d(TAG,"Get from server "+results.tracks.size() + " tracks.");

            return results.tracks;
        }catch (Exception e ){
            Log.e("", "error " + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Track> results) {
        super.onPostExecute(results);
        if (null != results && results.size()>0) {
            _d.onSuccessLoadTracks(results);
        }else{
            _d.onErrorLoadingTracks(null);
        }
    }
}
