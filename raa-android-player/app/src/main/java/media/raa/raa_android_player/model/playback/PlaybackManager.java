package media.raa.raa_android_player.model.playback;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;

import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PAUSE;
import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PLAY;
import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_RESUME;

/**
 * This class is responsible for communicating between the playback service and main app ui controls
 * Note that this class is in charge of in-app player bar and also playback buttons in the lists.
 * @see PlaybackService on the other hand controls the ExoPlayer instance and the notification bar controls
 * Created by hamid on 3/6/18.
 */

public class PlaybackManager {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String LIVE_BROADCAST_STREAM_URL = RaaContext.API_PREFIX_URL +
            "/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTFfbmV3Lm9nZw==";

    private PlayerStatus currentPlayerStatus = new PlayerStatus();
    private PlaybackManagerEventListener playbackManagerEventListener;

    private Context context;

    public PlaybackManager(Context context) {
        this.context = context;
        // todo Register for playback service event
    }

    public void playLiveBroadcast() {
        currentPlayerStatus.setEnabled(true);
        currentPlayerStatus.setPlaying(true);

        Program currentLiveProgram = RaaContext.getInstance().
                getLiveBroadcastLineup().getMostRecentProgram();

        currentPlayerStatus.setItemTitle(currentLiveProgram.getTitle());
        currentPlayerStatus.setItemTitle(currentLiveProgram.getSubtitle());

        ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                .getProgramInfoMap().get(currentLiveProgram.getProgramId());

        if (pInfo != null) {
            // View is responsible of handling default thumbnail
            currentPlayerStatus.setItemThumbnail(pInfo.getThumbnailBitmap());
        } else {
            currentPlayerStatus.setItemThumbnail(null);
        }

        currentPlayerStatus.setMediaSourceUrl(LIVE_BROADCAST_STREAM_URL);

        // notify in-app view and playback service
        if (playbackManagerEventListener != null) {
            playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
        }

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }

    public void playPublicFeedEntry(PublicFeedEntry entry) {
        if (entry != null) {
            // Update player status
            currentPlayerStatus.setEnabled(true);
            currentPlayerStatus.setPlaying(true);

            currentPlayerStatus.setItemTitle(entry.getProgram().getTitle());
            currentPlayerStatus.setItemSubtitle(entry.getProgram().getSubtitle());

            ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                    .getProgramInfoMap().get(entry.getProgram().getProgramId());

            if (pInfo != null) {
                // View is responsible of handling default thumbnail
                currentPlayerStatus.setItemThumbnail(pInfo.getThumbnailBitmap());
            } else {
                currentPlayerStatus.setItemThumbnail(null);
            }

            currentPlayerStatus.setMediaSourceUrl(entry.getProgram().getShow().getClips()[0].getMedia().getPath());

            // notify in-app view and playback service
            if (playbackManagerEventListener != null) {
                playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
            }

            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(ACTION_PLAY);
            context.startService(intent);
        }
    }

    public PlayerStatus getCurrentPlayerStatus() {
        return currentPlayerStatus;
    }

    //        }
    // If system notifications are not allowed, we cannot show the service controls,
    // therefore playback will be stopped upon quit
    // Also if user changed the settings to prevent background play
//        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()
//                || !RaaContext.getInstance().canPlayInBackground()) {
//        // If user does not allow notification, stop the service
//        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
//        intent.setAction(ACTION_STOP);
//        // Stop the playback
//        startService(intent);

//        if (playerBarActionButtonPlaying) {
//            playerBarActionButtonPlaying = false;
//            playerBarActionButton.setImageResource(R.drawable.ic_play_black_24dp);
//            Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
//            intent.setAction(ACTION_STOP);
//            startService(intent);
//        } else {
//            playerBarActionButtonPlaying = true;
//            playerBarActionButton.setImageResource(R.drawable.ic_pause_black_24dp);
//            Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
//            intent.setAction(ACTION_PLAY);
//            startService(intent);
//        }
//    }

    public void togglePlaybackState() {
        if (currentPlayerStatus != null && currentPlayerStatus.isPlaying) {
            this.pause();
        } else {
            this.resume();
        }

        if (playbackManagerEventListener != null) {
            playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
        }
    }

    private void pause() {
        currentPlayerStatus.isPlaying = false;

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    private void resume() {
        currentPlayerStatus.isPlaying = true;

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_RESUME);
        context.startService(intent);
    }

    public void setPlaybackManagerEventListener(PlaybackManagerEventListener playbackManagerEventListener) {
        this.playbackManagerEventListener = playbackManagerEventListener;
    }

    public interface PlaybackManagerEventListener {
        void onPlayerStatusChange(PlayerStatus newStatus);
    }

    public static class PlayerStatus {
        private boolean isEnabled;

        private String itemTitle;
        private String itemSubtitle;
        private Bitmap itemThumbnail;

        private String mediaSourceUrl;
        private boolean isPlaying;

        public boolean isEnabled() {
            return isEnabled;
        }

        void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public String getItemTitle() {
            return itemTitle;
        }

        void setItemTitle(String itemTitle) {
            this.itemTitle = itemTitle;
        }

        public String getItemSubtitle() {
            return itemSubtitle;
        }

        void setItemSubtitle(String itemSubtitle) {
            this.itemSubtitle = itemSubtitle;
        }

        public Bitmap getItemThumbnail() {
            return itemThumbnail;
        }

        void setItemThumbnail(Bitmap itemThumbnail) {
            this.itemThumbnail = itemThumbnail;
        }

        public String getMediaSourceUrl() {
            return mediaSourceUrl;
        }

        void setMediaSourceUrl(String mediaSourceUrl) {
            this.mediaSourceUrl = mediaSourceUrl;
        }

        public boolean isPlaying() {
            return isPlaying;
        }

        void setPlaying(boolean playing) {
            isPlaying = playing;
        }
    }
}
