package media.raa.raa_android_player.model.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import media.raa.raa_android_player.Player;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.PlaybackService;

import static media.raa.raa_android_player.model.PlaybackService.ACTION_PLAY;

public class NotificationService extends FirebaseMessagingService {

    private static final String ACTION_LISTEN = "action_listen";

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
                String alertText = remoteMessage.getData().get("alert");
                notificationManager.notify(RAA_CURRENTLY_PLAYING_NOTIFICATION_ID,
                        createNotification(alertText));
            } else { // silent notification, stop playback and remove all notifications
                // todo
            }
        }
    }

    public Notification createNotification(String alertText) {
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
                .setContentTitle("رادیو اتو-اسعد")
                .setContentText(alertText)
                .setContentIntent(appPendingIntent)
                .addAction(R.drawable.ic_raa_logo_round_24dp, "گوش می‌دهم", playPendingIntent);

        return notificationBuilder.build();
    }
}
