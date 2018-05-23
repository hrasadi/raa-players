package media.raa.raa_android_player.model.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.Show;
import media.raa.raa_android_player.model.entities.archive.ArchiveEntry;
import media.raa.raa_android_player.model.entities.feed.PersonalFeedEntry;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;

import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PLAY;
import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PAUSE;
import static media.raa.raa_android_player.model.playback.PlaybackService.ACTION_RESUME;

/**
 * This class is responsible for communicating between the playback service and main app ui controls
 * Note that this class is in charge of in-app player bar and also playback buttons in the lists.
 *
 * @see PlaybackService on the other hand controls the ExoPlayer instance and the notification bar controls
 * Created by hamid on 3/6/18.
 */

public class PlaybackManager {

    public static final String ACTION_TOGGLE_PLAYBACK = "media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_TOGGLE_PLAYBACK";
    public static final String ACTION_STOP = "media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_STOP";
    static final String ACTION_PLAYBACK_FINISHED = "media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_PLAYBACK_FINISHED";

    private static final String SETTINGS_PLAYBACK_STATE_KEY = "playbackState";

    @SuppressWarnings("SpellCheckingInspection")
    private static final String LIVE_BROADCAST_STREAM_URL = RaaContext.API_PREFIX_URL +
            "/linkgenerator/live.mp3?src=aHR0cHM6Ly9zdHJlYW0ucmFhLm1lZGlhL3JhYTFfbmV3Lm9nZw==";

    private PlayerStatus currentPlayerStatus = new PlayerStatus();
    private List<PlaybackManagerEventListener> playbackManagerEventListeners = new ArrayList<>();

    private Context context;
    private SharedPreferences settings;
    private Gson gson;

    public PlaybackManager(Context context, SharedPreferences settings) {
        this.context = context;
        this.settings = settings;

        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    }

    public void playLiveBroadcast() {
        currentPlayerStatus = new PlayerStatus();

        currentPlayerStatus.setProgramType(PlayerStatus.ProgramType.Live);

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
        this.notifyListeners();

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        context.startService(intent);
    }

    public void playPublicFeedEntry(PublicFeedEntry entry) {
        playPublicFeedEntry(entry, 0L);
    }

    public void playPublicFeedEntry(PublicFeedEntry entry, long playbackOffset) {
        currentPlayerStatus = new PlayerStatus();

        currentPlayerStatus.setProgramType(PlayerStatus.ProgramType.Public);

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
            this.notifyListeners();

            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(ACTION_PLAY);
            intent.putExtra("seekTo", playbackOffset);
            context.startService(intent);
        }
    }

    public void playPersonalFeedEntry(PersonalFeedEntry entry) {
        currentPlayerStatus = new PersonalEntryPlayerStatus();

        currentPlayerStatus.setProgramType(PlayerStatus.ProgramType.Personal);

        if (entry != null) {
            // Update player status
            currentPlayerStatus.setEnabled(true);
            currentPlayerStatus.setPlaying(true);

            long playbackOffset = this.updatePersonalEntryPlayerStatus(entry);

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

            if (((PersonalEntryPlayerStatus) currentPlayerStatus).isPlayingPreShow) {
                currentPlayerStatus.setMediaSourceUrl(entry.getProgram().getPreShow().getClips()[0].getMedia().getPath());
            } else {
                currentPlayerStatus.setMediaSourceUrl(entry.getProgram().getShow().getClips()[0].getMedia().getPath());
            }

            // notify in-app view and playback service
            this.notifyListeners();

            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(ACTION_PLAY);
            intent.putExtra("seekTo", playbackOffset);
            context.startService(intent);
        }
    }

    /**
     * Sets personal entry specific player status
     * @param personalFeedEntry requested personal entry
     * @return the offset of playback in millis
     */
    private long updatePersonalEntryPlayerStatus(PersonalFeedEntry personalFeedEntry) {
        PersonalEntryPlayerStatus personalEntryCurrentPlayerStatus = ((PersonalEntryPlayerStatus) currentPlayerStatus);
        personalEntryCurrentPlayerStatus.setPersonalFeedEntry(personalFeedEntry);

        double preShowDuration = 0;
        Show preShow = personalFeedEntry.getProgram().getPreShow();
        if (preShow != null) {
            preShowDuration = preShow.getClips()[0].getMedia().getDuration();
        }
        long showStartTimestampMillis = (personalFeedEntry.getReleaseTimestamp() + (long) preShowDuration) * 1000;

        long nowMillis = DateTime.now().getMillis();
        // Show is already started
        if (nowMillis > showStartTimestampMillis) {
            personalEntryCurrentPlayerStatus.setPlayingPreShow(false);
            return nowMillis - showStartTimestampMillis;
        } else {
            personalEntryCurrentPlayerStatus.setPlayingPreShow(true);
            return nowMillis - (personalFeedEntry.getReleaseTimestamp() * 1000);
        }
    }

    public void playArchiveEntry(ArchiveEntry entry) {
        this.playArchiveEntry(entry, 0L);
    }

    public void playArchiveEntry(ArchiveEntry entry, long playbackOffset) {
        currentPlayerStatus = new PlayerStatus();

        currentPlayerStatus.setProgramType(PlayerStatus.ProgramType.Archive);

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
            this.notifyListeners();

            Intent intent = new Intent(context, PlaybackService.class);
            intent.setAction(ACTION_PLAY);
            intent.putExtra("seekTo", playbackOffset);
            context.startService(intent);
        }
    }

    public void togglePlaybackState() {
        if (currentPlayerStatus != null && currentPlayerStatus.isPlaying) {
            this.pause();
        } else {
            this.resume();
        }

        if (playbackManagerEventListeners != null) {
            for (PlaybackManagerEventListener playbackManagerEventListener : playbackManagerEventListeners) {
                playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
            }
        }
    }

    private void handlePlaybackFinished() {
        if (currentPlayerStatus.getProgramType() == PlayerStatus.ProgramType.Personal) {
            if (((PersonalEntryPlayerStatus) currentPlayerStatus).isPlayingPreShow()) {
                // Move to show playback
                ((PersonalEntryPlayerStatus) currentPlayerStatus).setPlayingPreShow(false);
                currentPlayerStatus.setMediaSourceUrl(
                        ((PersonalEntryPlayerStatus) currentPlayerStatus).getPersonalFeedEntry()
                                .getProgram().getShow().getClips()[0].getMedia().getPath());
                // Start from the beginning of the show media file
                Intent intent = new Intent(context, PlaybackService.class);
                intent.setAction(ACTION_PLAY);
                intent.putExtra("seekTo", 0);
                context.startService(intent);
            } else {
                // Show was finished. Stop playback
                this.stop();
            }
        } else {
            this.removePlaybackState();

            // No more actions required! Hide the players and reset PlaybackService state
            this.stop();
        }
    }

    private void resume() {
        currentPlayerStatus.setPlaying(true);

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_RESUME);
        context.startService(intent);
    }

    private void pause() {
        // save playback state in settings
        this.savePlaybackState();

        currentPlayerStatus.setPlaying(false);

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PAUSE);
        context.startService(intent);
    }

    public void stop() {
        // save playback state in settings
        this.savePlaybackState();

        currentPlayerStatus.setPlaying(false);
        currentPlayerStatus.setEnabled(false);

        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(PlaybackService.ACTION_STOP);
        context.startService(intent);

        if (playbackManagerEventListeners != null) {
            for (PlaybackManagerEventListener playbackManagerEventListener : playbackManagerEventListeners) {
                playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
            }
        }
    }

    private void savePlaybackState() {
        // Stale UI buttons, nothing to do!
        if (this.getCurrentPlayerStatus() == null || this.getCurrentPlayerStatus().getProgramType() == null) {
            return;
        }

        if (this.getCurrentPlayerStatus().getProgramType().equals(PlayerStatus.ProgramType.Public) ||
                this.getCurrentPlayerStatus().getProgramType().equals(PlayerStatus.ProgramType.Archive)) {
            SharedPreferences.Editor editor = this.settings.edit();

            Map<String, Long> playbackStateDict;
            if (!this.settings.contains(SETTINGS_PLAYBACK_STATE_KEY)) {
                playbackStateDict = new HashMap<>();
            } else {
                playbackStateDict = gson.fromJson(
                        this.settings.getString(SETTINGS_PLAYBACK_STATE_KEY, null),
                        new TypeToken<HashMap<String, Long>>(){}.getType());
            }

            playbackStateDict.put(this.getCurrentPlayerStatus().getMediaSourceUrl(),
                    PlaybackService.getCurrentPlaybackPosition());

            editor.putString(SETTINGS_PLAYBACK_STATE_KEY, gson.toJson(playbackStateDict));
            editor.apply();
        }
    }

    private void removePlaybackState() {
        SharedPreferences.Editor editor = this.settings.edit();
        if (this.settings.contains(SETTINGS_PLAYBACK_STATE_KEY)) {
            Map<String, Long> playbackStateDict = gson.fromJson(
                    this.settings.getString(SETTINGS_PLAYBACK_STATE_KEY, null),
                    new TypeToken<HashMap<String, Long>>(){}.getType());

            // Remove any saved status for item
            playbackStateDict.remove(this.getCurrentPlayerStatus().getMediaSourceUrl());

            editor.putString(SETTINGS_PLAYBACK_STATE_KEY, gson.toJson(playbackStateDict));
            editor.apply();
        }
    }

    public long getLastPlaybackState(String mediaSourceUrl) {
        if (this.settings.contains(SETTINGS_PLAYBACK_STATE_KEY)) {
            Map<String, Long> playbackStateDict = gson.fromJson(
                    this.settings.getString(SETTINGS_PLAYBACK_STATE_KEY, null),
                    new TypeToken<HashMap<String, Long>>() {
                    }.getType());
            if (playbackStateDict.containsKey(mediaSourceUrl)) {
                return playbackStateDict.get(mediaSourceUrl);
            }
        }
        return 0;
    }

    private void notifyListeners() {
        if (playbackManagerEventListeners != null) {
            for (PlaybackManagerEventListener playbackManagerEventListener : playbackManagerEventListeners) {
                playbackManagerEventListener.onPlayerStatusChange(currentPlayerStatus);
            }
        }
    }

    public PlayerStatus getCurrentPlayerStatus() {
        return currentPlayerStatus;
    }

    public void registerPlaybackManagerEventListener(PlaybackManagerEventListener playbackManagerEventListener) {
        this.playbackManagerEventListeners.add(playbackManagerEventListener);
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

        private ProgramType programType;

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

        ProgramType getProgramType() {
            return programType;
        }

        void setProgramType(ProgramType programType) {
            this.programType = programType;
        }

        public enum ProgramType {
            Personal,
            Public,
            Live,
            Archive
        }
    }

    public static class PersonalEntryPlayerStatus extends PlayerStatus {
        private PersonalFeedEntry personalFeedEntry;
        private boolean isPlayingPreShow = false;

        PersonalFeedEntry getPersonalFeedEntry() {
            return personalFeedEntry;
        }

        void setPersonalFeedEntry(PersonalFeedEntry personalFeedEntry) {
            this.personalFeedEntry = personalFeedEntry;
        }

        boolean isPlayingPreShow() {
            return isPlayingPreShow;
        }

        void setPlayingPreShow(boolean playingPreShow) {
            isPlayingPreShow = playingPreShow;
        }
    }

    public static class PlaybackManagerBroadcastReceiver extends BroadcastReceiver {
        // Received broadcasts from PlayerService and NotificationBarPlayerControls and
        // calls appropriate PlaybackManager methods
        @Override
        public void onReceive(Context context, Intent intent) {
            // If context not exists, init, otherwise this call does nothing
            RaaContext.initializeInstance(context);

            if (Objects.equals(intent.getAction(), ACTION_TOGGLE_PLAYBACK)) {
                RaaContext.getInstance().getPlaybackManager().togglePlaybackState();
            } else if (Objects.equals(intent.getAction(), ACTION_STOP)) {
                RaaContext.getInstance().getPlaybackManager().stop();
            } else if (Objects.equals(intent.getAction(), ACTION_PLAYBACK_FINISHED)) {
                RaaContext.getInstance().getPlaybackManager().handlePlaybackFinished();
            }
        }
    }
}
