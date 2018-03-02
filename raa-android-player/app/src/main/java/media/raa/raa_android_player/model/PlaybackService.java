package media.raa.raa_android_player.model;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.Objects;

import media.raa.raa_android_player.RaaMainActivity;
import media.raa.raa_android_player.R;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static media.raa.raa_android_player.model.notification.NotificationService.RAA_CURRENTLY_PLAYING_NOTIFICATION_ID;

/**
 * Playback manager class implemented in the form of an Android IntentService.
  */
public class PlaybackService extends Service {

    private static final String STREAM_URL = "https://api.raa.media/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTFfbmV3Lm9nZw==";

    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_UPDATE_METADATA = "action_update_metadata";

    private static final int RAA_SERVICE_FOREGROUND_ID = 1;
    private static final int RAA_SERVICE_NOTIFICATION_ID = 1565;

    private MediaPlayer player;
    private MediaSessionCompat session;
    private MediaControllerCompat controller;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager notificationManager;
    private LocalBroadcastManager broadcaster;

    private MediaMetadataCompat.Builder metadataBuilder;

    private static boolean isInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        session = new MediaSessionCompat(getApplicationContext(), "RAA_SERVICE");
        session.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new PlaybackService.MediaSessionCallback());
        controller = session.getController();

        notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        notificationManager = ((NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE));

        broadcaster = LocalBroadcastManager.getInstance(this);

        metadataBuilder = new MediaMetadataCompat.Builder();

        // If the application is not running (and no context present) this is our chance to recreate
        // the RaaContext
        RaaContext.getInstance(getApplicationContext());
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

    protected void onHandleIntent(@Nullable Intent i) {
        if (i != null) {
            final Intent intent = i;
            // create a new thread
            AsyncTask.execute(() -> {
                if (Objects.equals(intent.getAction(), ACTION_PLAY)) {
                    updateMetadata();
                    // cancel new program notification (if any)
                    notificationManager.cancel(RAA_CURRENTLY_PLAYING_NOTIFICATION_ID);
                    controller.getTransportControls().play();
                } else if (Objects.equals(intent.getAction(), ACTION_STOP)) {
                    controller.getTransportControls().stop();
                } else if (Objects.equals(intent.getAction(), ACTION_PAUSE)) {
                    updateMetadata();
                    controller.getTransportControls().pause();
                }  else if (Objects.equals(intent.getAction(), ACTION_UPDATE_METADATA)) {
                    updateMetadata();
                    // todo encapsulate this logic
                    notificationManager.notify(RAA_SERVICE_FOREGROUND_ID, createNotification());
                }
                // In any case, update the player bar
                notifyUI();
            });
        }
    }

    private void updateMetadata() {
        // force read status from server
        RaaContext.getInstance().getCurrentStatus(true);

        // Now update the UI
        metadataBuilder.putText(MediaMetadataCompat.METADATA_KEY_ARTIST, "رادیو اتو-اسعد")
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART,
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_raa_logo))
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM,
                        (RaaContext.getInstance().getCurrentStatus(false).getCurrentClip() != null ?
                                RaaContext.getInstance().getCurrentStatus(false).getCurrentClip() : ""))
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE,
                        (RaaContext.getInstance().getCurrentStatus(false).getCurrentProgram() != null ?
                                RaaContext.getInstance().getCurrentStatus(false).getCurrentProgram():
                                "بخش بعدی برنامه\u200Cها به زودی"));

        session.setMetadata(metadataBuilder.build());
    }

    public Notification createNotification() {
        String actionToDisplay = ACTION_PLAY; // not playing
        int actionToDisplayIcon = R.drawable.ic_play_black_24dp;
        int shouldDisplayPauseButton = INVISIBLE;
        if (player != null && player.isPlaying()) {
            actionToDisplay = ACTION_STOP;
            actionToDisplayIcon = R.drawable.ic_stop_button;
            shouldDisplayPauseButton = VISIBLE;
        }

        Intent stopStartIntent = new Intent(getApplicationContext(), PlaybackService.class);
        stopStartIntent.setAction(actionToDisplay);
        PendingIntent stopStartPendingIntent = PendingIntent.getService(getApplicationContext(),
                RAA_SERVICE_NOTIFICATION_ID, stopStartIntent, 0);

        Intent pauseIntent = new Intent(getApplicationContext(), PlaybackService.class);
        pauseIntent.setAction(ACTION_PAUSE);
        PendingIntent pausePendingIntent = PendingIntent.getService(getApplicationContext(),
                RAA_SERVICE_NOTIFICATION_ID, pauseIntent, 0);

        RemoteViews remoteWidget = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_media_controller);
        remoteWidget.setOnClickPendingIntent(R.id.notification_action_start_stop, stopStartPendingIntent);
        remoteWidget.setOnClickPendingIntent(R.id.notification_action_pause, pausePendingIntent);

        remoteWidget.setImageViewResource(R.id.notification_action_start_stop, actionToDisplayIcon);
        remoteWidget.setViewVisibility(R.id.notification_action_pause, shouldDisplayPauseButton);

        remoteWidget.setTextViewText(R.id.notification_program_name_text, controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE));
        remoteWidget.setTextViewText(R.id.notification_program_clips_text, controller.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ALBUM));

        notificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_raa_logo_round_24dp)
                .setContent(remoteWidget);

        return notificationBuilder.build();
    }

    // This will notify the UI (to update the playback status of the bar)
    private void notifyUI() {
        if (RaaContext.getInstance().isApplicationForeground()) {
            Intent metadataUpdateIntent = new Intent(RaaMainActivity.PLAYER_BAR_EVENT);
            broadcaster.sendBroadcast(metadataUpdateIntent);
        }
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
            int result = am != null ? am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) : AudioManager.AUDIOFOCUS_NONE;

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                // Set the session active  (and update metadata and state)
                session.setActive(true);

                // player != null of user paused the stream
                if (player == null) {
                    player = new MediaPlayer();
                    try {
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(STREAM_URL);
                        // We are already running in a dedicated thread, so do this synchronously
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                player.start();
            }

            if (!isInForeground) {
                isInForeground = true;
                startForeground(RAA_SERVICE_FOREGROUND_ID, createNotification());
            } else { // it is already in foreground, so only update the notification
                notificationManager.notify(RAA_SERVICE_FOREGROUND_ID, createNotification());
            }
        }

        @Override
        public void onPause() {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }
            // Note: the onPause handler is called in an asynchronous manner. Therefore
            // we cannot call notify from outside this handler
            notificationManager.notify(RAA_SERVICE_FOREGROUND_ID, createNotification());
        }

        @Override
        public void onStop() {
            if (player != null) {
                player.stop();
                player.release();
                player = null;
            }

            session.setActive(false);
            session.release();

            isInForeground = false;

            stopForeground(false);
            stopSelf();
        }
    }

    public static boolean isPlaybackServiceActive() {
        return isInForeground;
    }

}
