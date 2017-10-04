package media.raa.raa_android_player.model;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v7.app.NotificationCompat;

import media.raa.raa_android_player.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener {
    private static final String STREAM_URL = "https://stream.raa.media/raa1.ogg";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_STOP = "action_stop";

    private MediaPlayer player;
    private MediaSessionCompat session;
    private MediaControllerCompat controller;

    private NotificationCompat.Builder notificationBuilder;
    private MediaMetadataCompat.Builder metadataBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        session = new MediaSessionCompat(getApplicationContext(), "RAA_SERVICE");
        session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSessionCallback());
        controller = session.getController();

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_PLAY)) {
            controller.getTransportControls().play();
        }

        return super.onStartCommand(intent, flags, startId);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        session.release();
        return super.onUnbind(intent);
    }

    private Notification createNotification() {

        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_ALBUM, "Salam");
        session.setMetadata(metadataBuilder.build());
        session.setActive(true);

        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        notificationBuilder.setStyle(new NotificationCompat.MediaStyle())
                .setContentText("salam")
                .setSmallIcon(R.drawable.ic_podcast_black_24dp)
                .addAction(new NotificationCompat.Action(R.drawable.ic_podcast_black_24dp, "Salam", pendingIntent));

        return notificationBuilder.build();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    private class MediaSessionCallback extends Callback {

        @Override
        public void onPlay() {

            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for playback, this registers the afChangeListener
            int result = am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Set the session active  (and update metadata and state)
                session.setActive(true);

                PlaybackService.this.startService(new Intent(getApplicationContext(), PlaybackService.class).setAction(ACTION_PLAY));
//                // start the player (custom call)
//                player.start();
                PlaybackService.this.startForeground(1223, createNotification());
            }
        }
//
//        @Override
//        public void onStop() {
//            PlaybackService.this.player.stop();
//            PlaybackService.this.stopForeground(false);
//        }
    }
}
