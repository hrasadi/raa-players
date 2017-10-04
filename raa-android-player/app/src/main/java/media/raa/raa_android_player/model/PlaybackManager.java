package media.raa.raa_android_player.model;

import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import java.io.IOException;

/**
 * This is where we manage the playback
 * Created by hamid on 10/2/17.
 */

public class PlaybackManager {
    private static final String STREAM_URL = "https://stream.raa.media/raa1.ogg";

    private MediaPlayer player = null;
    private MediaSessionCompat mSession = null;

    PlaybackManager() {
        mSession = new MediaSessionCompat(getApplicationContext(), "MusicService");
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    public void play() {
        if (player == null) {
            player = new MediaPlayer();
            try {
                player.setDataSource(STREAM_URL);
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        player.start();
                    }
                });
                player.prepareAsync();
            } catch (IOException e) {
                Log.e("Raa", "Error while initializing player", e);
            }
        } else {
            player.start();
        }
    }

    private void stop() {
        player.pause();
    }

}
