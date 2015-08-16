package com.gabriel.nanodegree.spotifystreamer.fragments;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gabriel.nanodegree.spotifystreamer.SpotifyStreamerApp;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;
import com.squareup.picasso.Picasso;
import com.gabriel.nanodegree.spotifystreamer.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlayerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends DialogFragment {

    private static String TAG = PlayerFragment.class.getSimpleName();

    private ImageView mImageView;
    private TextView mArtist, mSubtitle, mTitle, mTimeStart, mTimeEnd;
    private Button mPrevButton, mPlayButton, mNextButton;
    private SeekBar mSeekBar;
    private MediaPlayer mMediaPlayer;
    private SpotifyStreamerApp app;
    private int trackListPosition;
    private ProgressDialog mProgress;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param artistName Parameter 1.
     * @param albumName Parameter 2.
     * @param trackName Parameter 3.
     * @param trackImageURL Parameter 4.
     * @param trackPreviewURL Parameter 5.
     * @return A new instance of fragment PlayerFragment.
     */
    public static PlayerFragment newInstance(String artistName, String albumName, String trackName,String trackImageURL, String trackPreviewURL) {
        PlayerFragment fragment = new PlayerFragment();
        Bundle args = new Bundle();
        args.putString(Constants.ARTIST_NAME_KEY, artistName);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        app = (SpotifyStreamerApp) getActivity().getApplication();

        if (getArguments() != null) {
            for (int i = 0; i < app.trackList.size(); i++) {
                Track t = app.trackList.get(i);
                if (t.name.equals(getArguments().getString(Constants.TRACK_NAME_KEY))) {
                    trackListPosition = i;
                    break;
                }
            }
        }
        try {
            if (!app.mIsLargeLayout && null != ((ActionBarActivity) getActivity()).getSupportActionBar())
                ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ex) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        mProgress = new ProgressDialog(getActivity(), STYLE_NO_TITLE);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) mMediaPlayer.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mImageView = (ImageView) v.findViewById(R.id.image);
        mArtist = (TextView) v.findViewById(R.id.artist);
        mTitle = (TextView) v.findViewById(R.id.title);
        mSubtitle = (TextView) v.findViewById(R.id.subtitle);
        mTimeStart = (TextView) v.findViewById(R.id.timeStart);
        mTimeEnd = (TextView) v.findViewById(R.id.timeEnd);
        mPlayButton = (Button) v.findViewById(R.id.play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaPlayer.isPlaying()) {
                    Log.d(TAG, "Pause Music ");
                    mMediaPlayer.pause();
                    mPlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_play));
                } else {
                    Log.d(TAG, "Play Music ");
                    playMusic();
                }
            }
        });
        mPrevButton = (Button) v.findViewById(R.id.previous);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousMusic();
            }
        });
        mNextButton = (Button) v.findViewById(R.id.next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextMusic();
                }
            });
        setupMusic(app.trackList.get(trackListPosition));
        return v;
    }



    private void setupMusic(Track track){
        mProgress.show();
        mPrevButton.setVisibility(View.INVISIBLE);
        mPlayButton.setVisibility(View.INVISIBLE);
        mNextButton.setVisibility(View.INVISIBLE);

        mPlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_play));

        mArtist.setText(track.artists.get(0).name);
        mTitle.setText(track.album.name);
        mSubtitle.setText(track.name);
        mTimeStart.setText("0:00");
        mTimeEnd.setText("0:00");
        if (track.album.images.size() > 0 && track.album.images.get(0).url != null && !track.album.images.get(0).url.isEmpty()) {
            Picasso.with(this.getActivity()).load(track.album.images.get(0).url).into(mImageView);
        }
        try {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(track.preview_url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPrevButton.setVisibility(View.VISIBLE);
                    mPlayButton.setVisibility(View.VISIBLE);
                    mNextButton.setVisibility(View.VISIBLE);
                    if (trackListPosition == 0)
                        mPrevButton.setVisibility(View.INVISIBLE);
                    if (trackListPosition == app.trackList.size() - 1)
                        mNextButton.setVisibility(View.INVISIBLE);
                    mProgress.dismiss();
                    playMusic();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_play));
                    mTimeStart.setText("0:00");
                    mSeekBar.setProgress(0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            mProgress.dismiss();
        }
    }

    public void previousMusic(){
        if (trackListPosition > 0) {
            trackListPosition--;
            setupMusic(app.trackList.get(trackListPosition));
        }
    }

    public void playMusic(){
        try {
            if (mMediaPlayer.isPlaying()) {
                mPlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_play));
                mMediaPlayer.pause();
            }
            mMediaPlayer.start();
            mPlayButton.setBackground(getResources().getDrawable(android.R.drawable.ic_media_pause));
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mTimeEnd.setText("" + new SimpleDateFormat("m:ss").format(new Date(mMediaPlayer.getDuration())));
            myHandler.postDelayed(UpdateSongTime, 100);
        } catch (Exception e) {
            Log.e(TAG, "Play Music Error: "+e.getLocalizedMessage());
        }
    }

    public void nextMusic(){
        if (trackListPosition < app.trackList.size()-1) {
            trackListPosition++;
            setupMusic(app.trackList.get(trackListPosition));
        }
    }

    private Handler myHandler = new Handler();
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mTimeStart.setText("" + new SimpleDateFormat("m:ss").format(new Date(mMediaPlayer.getCurrentPosition())));
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                myHandler.postDelayed(this, 100);
            }
        }
    };

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }
}