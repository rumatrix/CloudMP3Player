package com.example.swat.mp3player;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import java.io.IOException;

public class MP3Player extends Service {

    private String listMusic[];
    private int trackId;
    public static MediaPlayer mediaPlayer;
    private Uri DATA_URI = Uri.parse("");
    private static final String DEFAULT_AUDIO_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/MP3Player/Music/";

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void onDestroy() {
        releaseMediaPlayer();
        stopService(new Intent(this, MP3Player.class));
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        listMusic = intent.getStringArrayExtra(getText(R.string.list_music).toString());
        trackId = intent.getIntExtra(getText(R.string.track_id).toString(), 0);

        DATA_URI = Uri.parse(DEFAULT_AUDIO_PATH + listMusic[trackId]);

        releaseMediaPlayer();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(String.valueOf(DATA_URI));
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return START_STICKY;
    }

    public void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
