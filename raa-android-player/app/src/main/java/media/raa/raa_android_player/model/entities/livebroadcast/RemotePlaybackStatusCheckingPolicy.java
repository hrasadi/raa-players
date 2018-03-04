package media.raa.raa_android_player.model.entities.livebroadcast;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import media.raa.raa_android_player.model.PlaybackService;
import media.raa.raa_android_player.model.RaaContext;

import static media.raa.raa_android_player.model.PlaybackService.ACTION_UPDATE_METADATA;

/**
 * The policy that shows how the server status is retrieved
 * Created by hamid on 10/15/17.
 */

public abstract class RemotePlaybackStatusCheckingPolicy {

    abstract public void init();
    abstract public void stop();

    public static class PollServerStatus extends RemotePlaybackStatusCheckingPolicy {

        private Context appContext;
        private Timer timer;

        public PollServerStatus(Context context) {
            Log.d("Raa", "Remote status check policy is: PollServerStatus");
            appContext = context;
        }

        @Override
        public void init() {
            // run every 10 seconds
            timer = new Timer("Raa-ReadServerStatusTimer");
            timer.schedule(new TimerTask() {
                private void initiateMetadataUpdate() {
                    // We are already in the app. So we only need to update the metadata
                    Intent updateMetadataIntent = new Intent(appContext, PlaybackService.class);
                    updateMetadataIntent.setAction(ACTION_UPDATE_METADATA);
                    appContext.startService(updateMetadataIntent);
                }

                @Override
                public void run() {
                    RemotePlaybackStatus oldStatus = RaaContext.getInstance().getCurrentStatus(false);
                    RemotePlaybackStatus newStatus = RaaContext.getInstance().getCurrentStatus(true);

                    if (oldStatus.isCurrentlyPlaying() != newStatus.isCurrentlyPlaying()) {
                        // Change in playback status is important.
                        initiateMetadataUpdate();
                    } else if (newStatus.isCurrentlyPlaying() &&
                           !oldStatus.getCurrentProgram().equals(newStatus.getCurrentProgram())) {
                        // New program started
                        initiateMetadataUpdate();
                    }
                }
            }, 0, 10000);
        }

        @Override
        public void stop() {
            timer.purge();
            timer.cancel();
        }
    }

    public static class ReceiveRemoteNotification extends RemotePlaybackStatusCheckingPolicy {

        public ReceiveRemoteNotification() {
            Log.d("Raa", "Remote status check policy is: ReceiveRemoteNotification");
        }

        @Override
        public void init() {
            // Do nothing, the NotificationService instance will be created by android
        }

        @Override
        public void stop() {
            // Do nothing, the NotificationService instance will destroy any resources
        }
    }
}

