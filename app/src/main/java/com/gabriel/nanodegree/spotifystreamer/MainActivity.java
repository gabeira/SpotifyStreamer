package com.gabriel.nanodegree.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.gabriel.nanodegree.spotifystreamer.fragments.ArtistsFragment;
import com.gabriel.nanodegree.spotifystreamer.fragments.PlayerFragment;
import com.gabriel.nanodegree.spotifystreamer.fragments.TopTracksFragment;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;

public class MainActivity extends ActionBarActivity implements ArtistsFragment.Callback, TopTracksFragment.Callback{

    private static final String TAG = MainActivity.class.getSimpleName();

    boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIsLargeLayout = getResources().getBoolean(R.bool.large_layout);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container1, ArtistsFragment.newInstance(), Constants.ARTISTS_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
            return true;
        }
        if (id == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArtistSelected(Artist artistSelected) {
        TopTracksFragment topTracksFragment = TopTracksFragment.newInstance(artistSelected.name ,artistSelected.id);
        if (mIsLargeLayout) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container2, topTracksFragment, Constants.TOPTRACKS_FRAGMENT_TAG)
                    .commit();
        }else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container1, topTracksFragment, Constants.TOPTRACKS_FRAGMENT_TAG)
                    .addToBackStack(Constants.TOPTRACKS_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public void onTrackSelected(Track trackSelected) {
        PlayerFragment playerFragment = PlayerFragment.newInstance(
                trackSelected.artists.get(0).name ,
                trackSelected.album.name ,
                trackSelected.name,
                trackSelected.album.images.get(0).url,
                trackSelected.preview_url);
        if (mIsLargeLayout) {
            playerFragment.show(getSupportFragmentManager(), "dialog");
        }else {
            getSupportFragmentManager().beginTransaction()
                .add(R.id.container1, playerFragment, Constants.PLAYER_FRAGMENT_TAG)
                .addToBackStack(Constants.PLAYER_FRAGMENT_TAG)
                .commit();
        }
    }
}