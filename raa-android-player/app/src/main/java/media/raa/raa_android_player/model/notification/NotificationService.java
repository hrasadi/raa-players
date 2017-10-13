package media.raa.raa_android_player.model.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import media.raa.raa_android_player.Player;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.PlaybackService;
import media.raa.raa_android_player.model.RaaContext;

import static media.raa.raa_android_player.model.PlaybackService.ACTION_PLAY;
import static media.raa.raa_android_player.model.PlaybackService.ACTION_STOP;
import static media.raa.raa_android_player.model.PlaybackService.ACTION_UPDATE_METADATA;

public class NotificationService extends FirebaseMessagingService {

    public static final int RAA_CURRENTLY_PLAYING_NOTIFICATION_ID = 2;

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager  notificationManager;

    public NotificationService() {
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        notificationBuilder = new NotificationCompat.Builder(this);
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().containsKey("alert")) {
                // New program
                handleNewProgram(remoteMessage.getData().get("alert"));
            } else { // silent notification, stop playback and remove all notifications
                handlePlaybackEnd();
            }
        }
    }

    private void handleNewProgram(String newProgramAlert) {
        if (RaaContext.getInstance(this).isApplicationForeground() ||
                PlaybackService.isPlaybackServiceActive()) {
            initiateMetadataUpdate();
        } else {
            if (RaaContext.getInstance(this).canSendNotifications()) {
                notificationManager.notify(RAA_CURRENTLY_PLAYING_NOTIFICATION_ID,
                        createNotification(newProgramAlert));
            }
        }
    }

    private void handlePlaybackEnd() {
        // remove all notifications (if any)
        notificationManager.cancel(RAA_CURRENTLY_PLAYING_NOTIFICATION_ID);

        if (RaaContext.getInstance(this).isApplicationForeground()) {
            // only update the metadata
            initiateMetadataUpdate();
        } else {
            // If in background, stop playback (only if it is already playing, this is to support
            // Android 8.0 changes)
            if (PlaybackService.isPlaybackServiceActive()) {
                Intent stopIntent = new Intent(getApplicationContext(), PlaybackService.class);
                stopIntent.setAction(ACTION_STOP);
                startService(stopIntent);
            }
        }

    }

    private Notification createNotification(String alertText) {
        Intent playIntent = new Intent(getApplicationContext(), PlaybackService.class);
        playIntent.setAction(ACTION_PLAY);
        PendingIntent playPendingIntent = PendingIntent.getService(getApplicationContext(),
                RAA_CURRENTLY_PLAYING_NOTIFICATION_ID, playIntent, 0);

        Intent appIntent = new Intent(getApplicationContext(), Player.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(getApplicationContext(),
                RAA_CURRENTLY_PLAYING_NOTIFICATION_ID, appIntent, 0);

        notificationBuilder
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_raa_logo_round_24dp)
                .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.program_start))
                .setContentTitle(alertText)
                .setContentIntent(appPendingIntent)
                .addAction(R.drawable.ic_raa_logo_round_24dp, "گوش می‌دهم", playPendingIntent);

        return notificationBuilder.build();
    }

    private void initiateMetadataUpdate() {
        // We are already in the app. So we only need to update the metadata
        Intent updateMetadataIntent = new Intent(getApplicationContext(), PlaybackService.class);
        updateMetadataIntent.setAction(ACTION_UPDATE_METADATA);
        startService(updateMetadataIntent);
    }
}
