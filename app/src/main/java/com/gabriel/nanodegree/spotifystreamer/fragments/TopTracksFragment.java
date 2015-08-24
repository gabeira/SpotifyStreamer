package com.gabriel.nanodegree.spotifystreamer.fragments;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gabriel.nanodegree.spotifystreamer.R;
import com.gabriel.nanodegree.spotifystreamer.SpotifyStreamerApp;
import com.gabriel.nanodegree.spotifystreamer.adapters.TracksListAdapter;
import com.gabriel.nanodegree.spotifystreamer.tasks.SpotifyTracksTask;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TopTracksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopTracksFragment
        extends Fragment
        implements SpotifyTracksTask.Delegate, TracksListAdapter.ClickListener {

    private String mParamArtistName;
    private String mParamArtistId;

    private String country_key;

    private RecyclerView recyclerView;
    private TracksListAdapter adapter;
    private ProgressDialog progress;
    private SpotifyStreamerApp app;

    private boolean loadRequired = true;

    public interface Callback {
        void onTrackSelected(int trackSelectedPosition);
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param artistName Parameter ArtistName.
     * @param artistId Parameter ArtistId.
     * @return A new instance of fragment TopTracksFragment.
     */
    public static TopTracksFragment newInstance(String artistName, String artistId) {
        TopTracksFragment fragment = new TopTracksFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARG_ARTIST_NAME_KEY, artistName);
        args.putString(Constants.ARG_ARTIST_ID_KEY, artistId);
        fragment.setArguments(args);
        return fragment;
    }

    public TopTracksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        if (mParamArtistName != null && !mParamArtistName.isEmpty()) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setSubtitle(mParamArtistName);
        }
        progress = new ProgressDialog(getActivity(), DialogFragment.STYLE_NO_TITLE);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        country_key = sharedPrefs.getString(
                getString(R.string.pref_country_key),
                getString(R.string.pref_country_default));

        recyclerView = (RecyclerView) v.findViewById(R.id.topListView);
        adapter = new TracksListAdapter(new ArrayList<Track>(10),this.getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter.setClickListener(this);

        if (getArguments() != null && loadRequired) {
            if (app.isOnline()) {
                progress.show();
                new SpotifyTracksTask(TopTracksFragment.this).execute(mParamArtistId, country_key);
                loadRequired=false;
            }else{
                Toast.makeText(getActivity(),getString(R.string.network_not_available),Toast.LENGTH_LONG).show();
            }
        }else if (app.trackList.size()>0) {
            adapter.updateList(app.trackList);
        }

        try {
            if (!app.mIsLargeLayout)
                ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (NullPointerException ex){}

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParamArtistName = getArguments().getString(Constants.ARG_ARTIST_NAME_KEY);
            mParamArtistId = getArguments().getString(Constants.ARG_ARTIST_ID_KEY);
        }
        app = (SpotifyStreamerApp) getActivity().getApplication();
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(null);
            ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } catch (NullPointerException ex) {
        }
    }

    @Override
    public void itemClicked(View view, int position) {
        if (adapter.getItemCount() >= position) {
            ((Callback) getActivity()).onTrackSelected(position);
        }
    }

    @Override
    public void onSuccessLoadTracks(List<Track> tracks) {
        app.trackList = tracks;
        adapter.updateList(tracks);
        progress.dismiss();
    }

    @Override
    public void onErrorLoadingTracks(Exception e) {
        progress.dismiss();
        Toast.makeText(getActivity(), getString(R.string.tracks_not_found), Toast.LENGTH_LONG).show();
    }
}
