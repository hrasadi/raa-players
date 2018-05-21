package media.raa.raa_android_player.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import media.raa.raa_android_player.model.entities.archive.Archive;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.livebroadcast.LiveBroadcastLineup;
import media.raa.raa_android_player.model.entities.programinfodirectory.ProgramInfoDirectory;
import media.raa.raa_android_player.model.playback.PlaybackManager;
import media.raa.raa_android_player.model.user.UserManager;

/**
 * Singleton container of Raa common
 * Created by hamid on 9/30/17.
 */

public class RaaContext {
    private static RaaContext instance;

    public static final String BASE_URL = "https://raa.media";
    public static final String API_PREFIX_URL = "https://api.raa.media";

    public static void initializeInstance(Context context) {
        if (instance == null && context != null) {
            instance = new RaaContext(context);
        }
    }

    public static RaaContext getInstance() {
        if (instance == null) {
            throw new RuntimeException("RaaContext must be initialized before being used, consider" +
                    " calling RaaContent.initializeInstance() in your Activity.Create method.");
        }
        return instance;
    }

    private LiveBroadcastLineup liveBroadcastLineup = new LiveBroadcastLineup();
    private Feed feed = new Feed();
    private Archive archive = new Archive();

    private UserManager userManager;
    private ProgramInfoDirectory programInfoDirectory = new ProgramInfoDirectory();

    private PlaybackManager playbackManager;

    private boolean isApplicationForeground = false;

    private RaaContext(Context context) {
        // Load settings from preference store
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        this.userManager = new UserManager(context, settings);
        this.playbackManager = new PlaybackManager(context, settings);
    }

    /**
     * The instance of ProgramInfoDirectory. A call to reload() function will refresh its data in an
     * async manner
     *
     * @return The programInfoDirectory instance (may not be populated)
     */
    public ProgramInfoDirectory getProgramInfoDirectory() {
        return programInfoDirectory;
    }

    /**
     * Returns the current lineup. The reload function must be called in order to populate data
     * @return The lineup instance (may not be populated)
     */
    public LiveBroadcastLineup getLiveBroadcastLineup() {
        return liveBroadcastLineup;
    }

    /**
     * Returns the current lineup. The reload function must be called in order to populate data
     * @return The lineup instance (may not be populated)
     */
    public Feed getFeed() {
        return feed;
    }

    /**
     * Returns the current archive directory. The reload function must be called in order to populate data
     * @return The archive directory instance (may not be populated)
     */
    public Archive getArchive() {
        return archive;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public PlaybackManager getPlaybackManager() {
        return playbackManager;
    }

    public void setApplicationForeground() {
        isApplicationForeground = true;
    }

    public void setApplicationBackground() {
        isApplicationForeground = false;
    }

    public boolean isApplicationForeground() {
        return isApplicationForeground;
    }
}
