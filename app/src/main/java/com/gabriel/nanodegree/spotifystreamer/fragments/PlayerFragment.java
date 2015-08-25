package com.gabriel.nanodegree.spotifystreamer.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gabriel.nanodegree.spotifystreamer.services.PlayerService;
import com.gabriel.nanodegree.spotifystreamer.SpotifyStreamerApp;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;
import com.squareup.picasso.Picasso;
import com.gabriel.nanodegree.spotifystreamer.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A simple {@link Fragment} subclass.
 * // * Use the {@link PlayerFragment#getInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayerFragment extends DialogFragment {

    private static String TAG = PlayerFragment.class.getSimpleName();

    private ImageView mImageView;
    private TextView mArtist, mSubtitle, mTitle, mTimeStart, mTimeEnd;
    private ToggleButton mPlayButton;
    private Button mPrevButton, mNextButton;
    private SeekBar mSeekBar;
    private static SpotifyStreamerApp app;
    private ProgressDialog mProgress;
    private ShareActionProvider mShareActionProvider;
    private Intent serviceIntent;
    private boolean mBufferBroadcastIsRegistered;
    private static final String MUSIC_SHARE_HASHTAG = " #SpotifyStreamerApp shared music ";
    private static PlayerFragment playerFragment;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A getInstance of fragment PlayerFragment.
     */
    public static PlayerFragment getInstance() {
        if (null == playerFragment)
            playerFragment = new PlayerFragment();
        return playerFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (SpotifyStreamerApp) getActivity().getApplication();
        setHasOptionsMenu(true);
        try {
            if (!app.mIsLargeLayout && null != ((ActionBarActivity) getActivity()).getSupportActionBar())
                ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);
        mSeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) PlayerService.getInstance().seekMusicTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mImageView = (ImageView) v.findViewById(R.id.image);
        mArtist = (TextView) v.findViewById(R.id.artist);
        mTitle = (TextView) v.findViewById(R.id.title);
        mSubtitle = (TextView) v.findViewById(R.id.subtitle);
        mTimeStart = (TextView) v.findViewById(R.id.timeStart);
        mTimeEnd = (TextView) v.findViewById(R.id.timeEnd);
        mPlayButton = (ToggleButton) v.findViewById(R.id.play);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPlayButton.isChecked()) {
                    PlayerService.getInstance().pauseMusic();
                } else {
                    PlayerService.getInstance().startMusic();
                }
//                handleSeekbarChanges();
                myHandler.postDelayed(UpdateSongTime, 100);
            }
        });
        mPrevButton = (Button) v.findViewById(R.id.previous);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getTrackSelectedPosition() > 0) {
                    serviceIntent.setAction("PREV");
                    getActivity().startService(serviceIntent);
                }
            }
        });
        mNextButton = (Button) v.findViewById(R.id.next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (app.getTrackSelectedPosition() < app.trackList.size() - 1) {
                    serviceIntent.setAction("NEXT");
                    getActivity().startService(serviceIntent);
                }
            }
        });

        if (app.mIsLargeLayout) {
            Button btShare = (Button) v.findViewById(R.id.bt_share);
            btShare.setVisibility(View.VISIBLE);
            btShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(createShareIntent());
                }
            });
        }
        if (null == serviceIntent)
            serviceIntent = new Intent(getActivity(), PlayerService.class);
        serviceIntent.setAction("PLAY");
        if (getArguments().getBoolean("selectedFromList",true)) {
            if (savedInstanceState != null) {
                updateTrackInfo();
            }else {
                getActivity().startService(serviceIntent);
            }
        } else {
            updateTrackInfo();
        }
        return v;
    }

    private void updateTrackInfo() {
        Track track = app.getCurrentTrack();
        Log.i(TAG, "Setup to play music " + track.name);
        mArtist.setText(track.artists.get(0).name);
        mTitle.setText(track.album.name);
        mSubtitle.setText(track.name);
        if (track.album.images.size() > 0 && track.album.images.get(0).url != null && !track.album.images.get(0).url.isEmpty()) {
            Picasso.with(this.getActivity()).load(track.album.images.get(0).url).into(mImageView);
        }
        myHandler.postDelayed(UpdateSongTime, 100);
    }

    private BroadcastReceiver broadcastBufferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int bufferingIntValue = intent.getIntExtra(Constants.BROADCAST_STATUS, 0);
            switch (bufferingIntValue) {
                case Constants.PLAYER_READYTOPLAY:
                    mPrevButton.setVisibility(View.VISIBLE);
                    mPlayButton.setVisibility(View.VISIBLE);
                    mNextButton.setVisibility(View.VISIBLE);
                    if (app.getTrackSelectedPosition() == 0)
                        mPrevButton.setVisibility(View.INVISIBLE);
                    if (app.getTrackSelectedPosition() == app.trackList.size() - 1)
                        mNextButton.setVisibility(View.INVISIBLE);
                    if (mProgress != null) mProgress.dismiss();
                    mPlayButton.setChecked(true);
//                    handleSeekbarChanges();
                    myHandler.postDelayed(UpdateSongTime, 100);
                    break;
                case Constants.PLAYER_LOADING:
                    updateTrackInfo();
                    mProgress = new ProgressDialog(getActivity(), STYLE_NO_TITLE);
//                    mProgress = ProgressDialog.show(getActivity(), "Buffering...", "Acquiring song " + app.getCurrentTrack().name, true);
                    mProgress.show();
                    break;
                case Constants.PLAYER_FINISHED:
                    mPlayButton.setChecked(false);
                    mTimeStart.setText("0:00");
                    mSeekBar.setProgress(0);
                    break;
                case Constants.PLAYER_PAUSED:
                    mPlayButton.setChecked(false);
                    break;
            }
        }
    };

    private Handler myHandler = new Handler();
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            if (PlayerService.getInstance().isPlaying()) {
                updateSeekbarStatus();
                myHandler.postDelayed(this, 1000);
            }
        }
    };

//    public void handleSeekbarChanges() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (PlayerService.getInstance().isPlaying()) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        return;
//                    } catch (Exception e) {
//                        return;
//                    }
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateSeekbarStatus();
//                        }
//                    });
//                }
//                return;
//            }
//        }).start();
//    }

    private void updateSeekbarStatus() {
        int trackDuration = PlayerService.getInstance().getMusicDuration();
        mTimeEnd.setText("" + new SimpleDateFormat("m:ss").format(new Date(trackDuration)));
        mSeekBar.setMax(trackDuration);

        int currentPosition = PlayerService.getInstance().getCurrentPosition();
        mTimeStart.setText("" + new SimpleDateFormat("m:ss").format(new Date(currentPosition)));
        mSeekBar.setProgress(currentPosition);
    }

    @Override
    public void onPause() {
        // Unregister broadcast receiver
        if (mBufferBroadcastIsRegistered) {
            getActivity().unregisterReceiver(broadcastBufferReceiver);
            mBufferBroadcastIsRegistered = false;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // Register broadcast receiver
        if (!mBufferBroadcastIsRegistered) {
            getActivity().registerReceiver(broadcastBufferReceiver, new IntentFilter(PlayerService.BROADCAST_BUFFER));
            mBufferBroadcastIsRegistered = true;
        }
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (null != mShareActionProvider) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, MUSIC_SHARE_HASHTAG +
                app.getCurrentTrack().external_urls.get("spotify"));
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            startActivity(createShareIntent());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}