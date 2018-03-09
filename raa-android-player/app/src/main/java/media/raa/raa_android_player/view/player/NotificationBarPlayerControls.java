package media.raa.raa_android_player.view.player;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.playback.PlaybackManager;

import static media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_STOP;
import static media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_TOGGLE_PLAYBACK;

/**
 * Notification bar and lock screen controls
 * Created by hamid on 3/6/18.
 */

public class NotificationBarPlayerControls {
    private static final int RAA_SERVICE_NOTIFICATION_ID = 1565;

    private Context context;

    @SuppressWarnings("deprecation")
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManagerCompat notificationManager;
    private Notification currentNotification;

    public NotificationBarPlayerControls(Context context) {
        this.context = context;

        //noinspection deprecation
        notificationBuilder = new NotificationCompat.Builder(this.context);
        notificationManager = NotificationManagerCompat.from(this.context);
    }

    public boolean areNotificationEnabled() {
        return notificationManager.areNotificationsEnabled();
    }

    public void updateNotificationBarPlayerControls(PlaybackManager.PlayerStatus playerStatus) {
        if (playerStatus != null) {
            if (!playerStatus.isEnabled()) {
                // Remove the notification
                notificationManager.cancel(RAA_SERVICE_NOTIFICATION_ID);
            } else {
                createNotification(playerStatus);
                notificationManager.notify(RAA_SERVICE_NOTIFICATION_ID, this.currentNotification);
            }
        }
    }

    private void createNotification(PlaybackManager.PlayerStatus playerStatus) {
        RemoteViews remoteWidget = new RemoteViews(context.getPackageName(), R.layout.notification_bar_player_controls);

        // Close button
        Intent stopPlaybackIntent = new Intent(ACTION_STOP);
        PendingIntent stopPlaybackPendingIntent = PendingIntent.getBroadcast(context,
                1, stopPlaybackIntent, 0);

        remoteWidget.setOnClickPendingIntent(R.id.notification_bar_player_cancel_button, stopPlaybackPendingIntent);
        remoteWidget.setImageViewResource(R.id.notification_bar_player_cancel_button, R.drawable.ic_close_black_24dp);

        // Play/pause button
        Intent togglePlaybackIntent = new Intent(ACTION_TOGGLE_PLAYBACK);
        PendingIntent togglePlaybackPendingIntent = PendingIntent.getBroadcast(context,
                1, togglePlaybackIntent, 0);

        remoteWidget.setOnClickPendingIntent(R.id.notification_bar_player_action_button, togglePlaybackPendingIntent);
        remoteWidget.setImageViewResource(R.id.notification_bar_player_action_button, playerStatus.isPlaying() ?
                R.drawable.ic_pause_black_24dp : R.drawable.ic_play_black_24dp);

        remoteWidget.setTextViewText(R.id.notification_bar_player_program_title, playerStatus.getItemTitle());
        remoteWidget.setTextViewText(R.id.notification_bar_player_program_subtitle, playerStatus.getItemSubtitle());
        if (playerStatus.getItemThumbnail() != null) {
            remoteWidget.setImageViewBitmap(R.id.notification_bar_player_program_thumbnail, playerStatus.getItemThumbnail());
        } else {
            remoteWidget.setImageViewResource(R.id.notification_bar_player_program_thumbnail, R.drawable.img_default_thumbnail);
        }

        notificationBuilder
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_raa_logo_round_24dp)
                .setContent(remoteWidget);

        this.currentNotification = notificationBuilder.build();
    }

    public Notification getCurrentNotification() {
        return this.currentNotification;
    }
}
