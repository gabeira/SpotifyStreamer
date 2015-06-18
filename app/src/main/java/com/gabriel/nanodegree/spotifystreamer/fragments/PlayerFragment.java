package com.gabriel.nanodegree.spotifystreamer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gabriel.nanodegree.spotifystreamer.util.Constants;
import com.squareup.picasso.Picasso;
import com.gabriel.nanodegree.spotifystreamer.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends Fragment {

    private String albumName;
    private String trackName;
    private String trackPreviewURL;
    private String trackImageURL;

    private ImageView imageView;
    private TextView subtitle, title, timeStart, timeEnd;
    private Button previousButton, playButton, nextButton;
    private SeekBar seekBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param albumName Parameter 1.
     * @param trackName Parameter 2.
     * @param trackImageURL Parameter 3.
     * @param trackPreviewURL Parameter 4.
     * @return A new instance of fragment PlayerFragment.
     */
    public static PlayerFragment newInstance(String albumName, String trackName,String trackImageURL, String trackPreviewURL) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ALBUM_NAME_KEY, albumName);
        args.putString(Constants.TRACK_NAME_KEY, trackName);
        args.putString(Constants.TRACK_IMAGE_URL, trackImageURL);
        args.putString(Constants.TRACK_PREVIEW_URL, trackPreviewURL);
        fragment.setArguments(args);
        return fragment;
    }

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        seekBar = (SeekBar) v.findViewById(R.id.seekBar);

        imageView = (ImageView) v.findViewById(R.id.image);

        title = (TextView) v.findViewById(R.id.title);
        title.setText(albumName);

        subtitle = (TextView) v.findViewById(R.id.subtitle);
        subtitle.setText(trackName);

        if (trackImageURL != null && !trackImageURL.isEmpty()) {
            Picasso.with(this.getActivity()).load(trackImageURL).into(imageView);
        }
        timeStart = (TextView) v.findViewById(R.id.timeStart);
        timeEnd = (TextView) v.findViewById(R.id.timeEnd);
        playButton = (Button) v.findViewById(R.id.play);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            this.albumName = getArguments().getString(Constants.ALBUM_NAME_KEY);
            this.trackName = getArguments().getString(Constants.TRACK_NAME_KEY);
            this.trackImageURL = getArguments().getString(Constants.TRACK_IMAGE_URL);
            this.trackPreviewURL = getArguments().getString(Constants.TRACK_PREVIEW_URL);
        }
        if (null!= ((ActionBarActivity)getActivity()).getSupportActionBar())
            ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}