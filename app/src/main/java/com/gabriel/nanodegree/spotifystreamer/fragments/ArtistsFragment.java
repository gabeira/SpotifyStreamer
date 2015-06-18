package com.gabriel.nanodegree.spotifystreamer.fragments;


import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gabriel.nanodegree.spotifystreamer.R;
import com.gabriel.nanodegree.spotifystreamer.SpotifyStreamerApp;
import com.gabriel.nanodegree.spotifystreamer.adapters.ArtistsListAdapter;
import com.gabriel.nanodegree.spotifystreamer.tasks.SpotifyArtistsTask;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistsFragment extends Fragment implements SpotifyArtistsTask.Delegate, ArtistsListAdapter.ClickListener{

    private static final String TAG = ArtistsFragment.class.getSimpleName();

    SearchView searchArtist;
    RecyclerView recyclerView;
    private ArtistsListAdapter adapter;
    private ProgressDialog progress;
    private SpotifyStreamerApp app;
    private String country_key;

    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        return fragment;
    }

    public ArtistsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artists, container, false);

        progress = new ProgressDialog(getActivity());
        progress.setMessage(getString(R.string.loading));

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        country_key = sharedPrefs.getString(
                getString(R.string.pref_country_key),
                getString(R.string.pref_country_default));

        searchArtist= (SearchView) v.findViewById(R.id.searchArtist);
        searchArtist.setIconified(false);
        searchArtist.setQueryHint(getString(R.string.search_artist_name));
        searchArtist.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchArtist.clearFocus();
                progress.show();
                new SpotifyArtistsTask(ArtistsFragment.this).execute(query, country_key);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        adapter = new ArtistsListAdapter(new ArrayList<Artist>(0),getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.setClickListener(this);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        app = (SpotifyStreamerApp) getActivity().getApplication();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (app.artistList.size()>0){
            adapter.updateList(app.artistList);
        }else if(adapter.getItemCount()<1) {
            new SpotifyArtistsTask(ArtistsFragment.this).execute("*a*",country_key);
            progress.show();
        }
    }

    @Override
    public void onSuccessLoadArtists(List<Artist> artists) {
        app.artistList = artists;
        adapter.updateList(artists);
        progress.dismiss();
    }

    @Override
    public void onErrorLoadingArtists(Exception e) {
        progress.dismiss();
        adapter.updateList(new ArrayList<Artist>(0));
        Toast.makeText(getActivity(), getString(R.string.artist_not_found), Toast.LENGTH_LONG).show();
    }

    @Override
    public void itemClicked(View view, int position) {
        searchArtist.clearFocus();
        if (adapter.getItemCount()>=position) {
            TopTracksFragment topTracksFragment = TopTracksFragment.newInstance(adapter.getItem(position).name ,adapter.getItem(position).id);
            getActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, topTracksFragment, Constants.TOPTRACKS_FRAGMENT_TAG)
                        .addToBackStack(Constants.TOPTRACKS_FRAGMENT_TAG)
                        .commit();
        }
    }
}