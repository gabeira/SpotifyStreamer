package com.gabriel.nanodegree.spotifystreamer.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;

/**
 * Created by gabriel on 6/9/15.
 */
public class SpotifyArtistsTask extends AsyncTask<String,Void,List<Artist>>{

    private static final String TAG = SpotifyArtistsTask.class.getSimpleName();

    private Delegate _d;

    public interface Delegate {
        void onSuccessLoadArtists(List<Artist> artists);
        void onErrorLoadingArtists(Exception e);
    }

    public SpotifyArtistsTask(Delegate d) {
        this._d = d;
    }

    @Override
    protected List<Artist> doInBackground(final String... params) {
        try {
            if(null == params || params.length<2){
                return null;
            }
            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            final Map<String, Object> options = new HashMap<String, Object>() {
                {
                    put("market",params[1].toUpperCase());
                }
            };
            ArtistsPager results = spotify.searchArtists(params[0],options);
            Log.d(TAG,"Get from server "+results.artists.items.size() + " artists.");
            return results.artists.items;
        }catch (Exception e ){
            Log.e("", "error " + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Artist> results) {
        super.onPostExecute(results);
        if (null != results && results.size()>0) {
            _d.onSuccessLoadArtists(results);
        }else{
            _d.onErrorLoadingArtists(null);
        }
    }
}
