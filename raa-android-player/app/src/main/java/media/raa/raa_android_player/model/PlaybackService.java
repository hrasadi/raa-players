package media.raa.raa_android_player.model;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;

import java.io.IOException;

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
    public static final String ACTION_UPDATE_METADATA = "action_update_metadata";

    private static final int RAA_SERVICE_FOREGROUND_ID = 3399;
    private static final int RAA_SERVICE_NOTIFICATION_ID = 1;

    private MediaPlayer player;
    private MediaSessionCompat session;
    private MediaControllerCompat controller;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private MediaMetadataCompat.Builder metadataBuilder;


    private boolean isInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
        session = new MediaSessionCompat(getApplicationContext(), "RAA_SERVICE");
        session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new PlaybackService.MediaSessionCallback());
        controller = session.getController();

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        metadataBuilder = new MediaMetadataCompat.Builder();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(ACTION_PLAY)) {
                updateMetadata();
                controller.getTransportControls().play();
            } else if (intent.getAction().equals(ACTION_STOP)) {
                controller.getTransportControls().stop();
            } else if (intent.getAction().equals(ACTION_UPDATE_METADATA)) {
                updateMetadata();
                notificationManager.notify(RAA_SERVICE_NOTIFICATION_ID, createNotification());
            }
        }
    }

    private void updateMetadata() {
        // Update the meta data
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_ARTIST, "رادیو اتو-اسعد")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_logo))
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM,
                        (RaaContext.getInstance().getLineup().getCurrentProgram() != null ?
                                RaaContext.getInstance().getLineup().getCurrentProgram().programClips : ""))
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE,
                        (RaaContext.getInstance().getLineup().getCurrentProgram() != null ?
                                RaaContext.getInstance().getLineup().getCurrentProgram().programName:
                                "بخش بعدی برنامه\u200Cها به زودی"));

        session.setMetadata(metadataBuilder.build());
    }

    public Notification createNotification() {
        String actionToDisplay = ACTION_PLAY; // not playing
        int actionToDisplayIcon = R.drawable.ic_play_arrow_black_24dp;
        String actionLabel = "PLAY";
        if (player != null && player.isPlaying()) {
            actionToDisplay = ACTION_STOP;
            actionToDisplayIcon = R.drawable.ic_stop_black_24dp;
            actionLabel = "STOP";
        }

        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
        intent.setAction(actionToDisplay);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),
                RAA_SERVICE_NOTIFICATION_ID, intent, 0);

        notificationBuilder.setStyle(new NotificationCompat.MediaStyle())
                .setOngoing(true)
                .setContentTitle(controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE))
                .setContentText(controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
                .setSmallIcon(R.drawable.ic_raa_logo_round_24dp)
                .addAction(new NotificationCompat.Action(actionToDisplayIcon, actionLabel, pendingIntent));

        return notificationBuilder.build();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {

            // If we are already playing do nothing!
            if (player != null && player.isPlaying()) {
                return;
            }

            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            // Request audio focus for playback, this registers the afChangeListener
            int result = am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Set the session active  (and update metadata and state)
                session.setActive(true);

                player = new MediaPlayer();
                try {
                    player.setDataSource(STREAM_URL);
                    player.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                player.start();
            }

            if (!isInForeground) {
                isInForeground = true;
                startForeground(RAA_SERVICE_FOREGROUND_ID, createNotification());
            }
        }

        @Override
        public void onStop() {
            if (player != null) {
                player.stop();
            }
            player = null;

            isInForeground = false;

            stopForeground(false);
            stopSelf();
        }
    }

}
