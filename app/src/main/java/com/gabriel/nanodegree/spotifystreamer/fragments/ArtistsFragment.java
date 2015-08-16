package com.gabriel.nanodegree.spotifystreamer.fragments;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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

    public interface Callback {
        void onArtistSelected(Artist artistSelected);
    }

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

        progress = new ProgressDialog(getActivity(), DialogFragment.STYLE_NO_TITLE);

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
                if (app.isOnline()) {
                    searchArtist.clearFocus();
                    progress.show();
                    new SpotifyArtistsTask(ArtistsFragment.this).execute(query, country_key);
                }else{
                    Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_LONG).show();
                }
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
        recyclerView.scrollToPosition(adapter.getItemCount()-1);
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

            //Else if, it is on Debuggable Mode search for artists with "a"
        }else if(app.isOnline()
                && (0 != (getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))) {
            new SpotifyArtistsTask(ArtistsFragment.this).execute("*a*", country_key);
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
        if (adapter.getItemCount() >= position) {
            ((Callback) getActivity()).onArtistSelected(adapter.getItem(position));
        }
    }
}