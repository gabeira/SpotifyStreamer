package com.gabriel.nanodegree.spotifystreamer.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.gabriel.nanodegree.spotifystreamer.MainActivity;
import com.gabriel.nanodegree.spotifystreamer.R;
import com.gabriel.nanodegree.spotifystreamer.SpotifyStreamerApp;
import com.gabriel.nanodegree.spotifystreamer.util.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {

    private static final String TAG = "PlayerService";

    private static final String ACTION_PLAY = "PLAY";
    private static final String ACTION_PLAY_NOTIFICATION = "PLAY_NOTIFICATION";
    private static final String ACTION_PAUSE = "PAUSE";
    private static final String ACTION_PREV = "PREV";
    private static final String ACTION_NEXT = "NEXT";
    private static PlayerService mInstance = null;
    private static MediaPlayer mMediaPlayer = new MediaPlayer();    // The Media Player
    private static SpotifyStreamerApp app;

    private NotificationManager mNotificationManager;
    public static final String BROADCAST_BUFFER = "com.gabriel.nanodegree.spotifystreamer.services.broadcastbuffer";
    private Intent bufferIntent = new Intent(BROADCAST_BUFFER);
    private static final int PLAYER_NOTIFICATION_ID = 123;

    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private int headsetSwitch = 1;
    private NotificationCompat.Builder mBuilder;
    private NotificationCompat.Action actionPlay;

    public static PlayerService getInstance() {
        return mInstance;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        app = (SpotifyStreamerApp) getApplication();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.reset();
        mInstance = this;
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handlePhoneCall();
        if (intent.getAction().equals(ACTION_PLAY)) {
            setupMusic();
        } else if (intent.getAction().equals(ACTION_PLAY_NOTIFICATION)) {
            startMusic();
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            pauseMusic();
        } else if (intent.getAction().equals(ACTION_PREV)) {
            if (app.getTrackSelectedPosition() > 0) {
                app.setTrackSelectedPosition(app.getTrackSelectedPosition() - 1);
                setupMusic();
            }
        } else if (intent.getAction().equals(ACTION_NEXT)) {
            if (app.getTrackSelectedPosition() < app.trackList.size() - 1) {
                app.setTrackSelectedPosition(app.getTrackSelectedPosition() + 1);
                setupMusic();
            }
        }
        return START_STICKY;
    }

    private void setupMusic() {
        try {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(app.getCurrentTrack().preview_url);
            //SendBufferingBroadcast();
            bufferIntent.putExtra(Constants.BROADCAST_STATUS, Constants.PLAYER_LOADING);
            sendBroadcast(bufferIntent);
            mMediaPlayer.prepareAsync(); // prepare async to not block main thread
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePhoneCall() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseMusic();
                            isPausedInCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mMediaPlayer != null) {
                            if (isPausedInCall) {
                                isPausedInCall = false;
                                startMusic();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
        private boolean headsetConnected = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")) {
                if (headsetConnected && intent.getIntExtra("state", 0) == 0) {
                    headsetConnected = false;
                    headsetSwitch = 0;
                } else if (!headsetConnected && intent.getIntExtra("state", 0) == 1) {
                    headsetConnected = true;
                    headsetSwitch = 1;
                }
            }
            switch (headsetSwitch) {
                case 0:
                    if (mMediaPlayer.isPlaying()) {
                        pauseMusic();
                        bufferIntent.putExtra(Constants.BROADCAST_STATUS, Constants.PLAYER_PAUSED);
                        sendBroadcast(bufferIntent);
                    }
                    break;
                case 1:
                    break;
            }
        }
    };

    /**
     * Called when MediaPlayer is ready
     */
    @Override
    public void onPrepared(MediaPlayer player) {
        //SendBufferCompleteBroadcast();
        bufferIntent.putExtra(Constants.BROADCAST_STATUS, Constants.PLAYER_READYTOPLAY);
        sendBroadcast(bufferIntent);
        if (!mMediaPlayer.isPlaying()) this.startMusic();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
        bufferIntent.putExtra(Constants.BROADCAST_STATUS, Constants.PLAYER_FINISHED);
        sendBroadcast(bufferIntent);
        cancelNotification();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), TAG + " Error:" + extra, Toast.LENGTH_LONG).show();
        cancelNotification();
        return false;
    }

    public void pauseMusic() {
        if (null != mBuilder) {
            mBuilder.mActions.set(1, actionPlay);
            mNotificationManager.notify(PLAYER_NOTIFICATION_ID, mBuilder.build());
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void startMusic() {
        if (app.isOnline()) {
            mMediaPlayer.start();
            showNotification();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public int getMusicDuration() {
        return mMediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    public void seekMusicTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    private void showNotification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPrefs.getBoolean(getString(R.string.pref_notification_key), true)) {

                    Intent playIntent = new Intent(getApplicationContext(), PlayerService.class);
                    playIntent.setAction("PLAY_NOTIFICATION");
                    PendingIntent pIntentPlay = PendingIntent.getService(getApplicationContext(), 0, playIntent, 0);

                    Intent pauseIntent = new Intent(getApplicationContext(), PlayerService.class);
                    pauseIntent.setAction("PAUSE");
                    PendingIntent pIntentPause = PendingIntent.getService(getApplicationContext(), 0, pauseIntent, 0);

                    Intent prevIntent = new Intent(getApplicationContext(), PlayerService.class);
                    prevIntent.setAction("PREV");
                    PendingIntent pIntentPrev = PendingIntent.getService(getApplicationContext(), 0, prevIntent, 0);

                    Intent nextIntent = new Intent(getApplicationContext(), PlayerService.class);
                    nextIntent.setAction("NEXT");
                    PendingIntent pIntentNext = PendingIntent.getService(getApplicationContext(), 0, nextIntent, 0);

                    actionPlay = new NotificationCompat.Action(android.R.drawable.ic_media_play, "Play", pIntentPlay);

                    // Create the style object with BigPictureStyle subclass.
                    NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
                    notiStyle.setBigContentTitle("" + app.getCurrentTrack().name);
                    notiStyle.setSummaryText("" + app.getCurrentTrack().artists.get(0).name + " " + app.getCurrentTrack().album.name);
                    Bitmap remote_picture = null;
                    try {
                        remote_picture = BitmapFactory.decodeStream((InputStream) new URL(app.getCurrentTrack().album.images.get(0).url).getContent());
                    } catch (IOException e) {
                    }
                    notiStyle.bigPicture(remote_picture);

                    try {
                        mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                .setColor(getApplicationContext().getResources().getColor(R.color.spotify_green))
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setStyle(notiStyle)
                                .addAction(android.R.drawable.ic_media_previous, "Prev", pIntentPrev)
                                .addAction(android.R.drawable.ic_media_pause, "Pause", pIntentPause)
                                .addAction(android.R.drawable.ic_media_next, "Next", pIntentNext)
                                .setContentTitle("" + app.getCurrentTrack().name)
                                .setContentText("" + app.getCurrentTrack().artists.get(0).name + " " + app.getCurrentTrack().album.name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);

                    // The stack builder object will contain an artificial back stack for the started Activity.
                    // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    mNotificationManager.notify(PLAYER_NOTIFICATION_ID, mBuilder.build());
                }
                return;
            }
        }).start();
    }

    private void cancelNotification() {
        mNotificationManager.cancel(PLAYER_NOTIFICATION_ID);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.release();
            cancelNotification();
        }
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(headsetReceiver);
        //Broadcast finish to play
        bufferIntent.putExtra(Constants.BROADCAST_STATUS, Constants.PLAYER_FINISHED);
        sendBroadcast(bufferIntent);
    }
}