package media.raa.raa_android_player.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;

import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.livebroadcast.LiveBroadcastLineup;
import media.raa.raa_android_player.model.entities.livebroadcast.RemotePlaybackStatus;
import media.raa.raa_android_player.model.entities.livebroadcast.RemotePlaybackStatusCheckingPolicy;
import media.raa.raa_android_player.model.entities.programinfodirectory.ProgramInfoDirectory;
import media.raa.raa_android_player.model.user.UserManager;

/**
 * Singleton container of Raa common
 * Created by hamid on 9/30/17.
 */

public class RaaContext {
    private static RaaContext instance;

    public static final String BASE_URL = "https://raa.media";
    public static final String API_PREFIX_URL = "http://api.raa.media:7800";

    public static void initializeInstance(Context context) {
        instance = new RaaContext(context);
    }

    public static RaaContext getInstance() {
        if (instance == null) {
            throw new RuntimeException("RaaContext must be initialized before being used, consider" +
                    " calling RaaContent.initializeInstance() in your Activity.Create method.");
        }
        return instance;
    }

    public static RaaContext getInstance(Context context) {
        if (instance == null && context != null) {
            initializeInstance(context);
        }
        return getInstance();
    }

    private LiveBroadcastLineup liveBroadcastLineup = new LiveBroadcastLineup();
    private Feed feed = new Feed();
    private UserManager userManager;
    private RemotePlaybackStatus currentStatus = new RemotePlaybackStatus();
    private ProgramInfoDirectory programInfoDirectory = new ProgramInfoDirectory();
    private SharedPreferences settings;

    private RemotePlaybackStatusCheckingPolicy statusCheckingPolicy;

    private boolean isApplicationForeground = false;

    private RaaContext(Context context) {
        // Load settings from preference store
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);

        // If notifications are disabled, fallback to timer (in foreground mode only)
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            statusCheckingPolicy = new RemotePlaybackStatusCheckingPolicy.ReceiveRemoteNotification();
        } else {
            statusCheckingPolicy = new RemotePlaybackStatusCheckingPolicy.PollServerStatus(context);
        }

        this.userManager = new UserManager(context, settings);
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
     *
     * @return The lineup instance (may not be populated)
     */
    public LiveBroadcastLineup getLiveBroadcastLineup() {
        return liveBroadcastLineup;
    }

    /**
     * Returns the current lineup. The reload function must be called in order to populate data
     *
     * @return The lineup instance (may not be populated)
     */
    public Feed getFeed() {
        return feed;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RemotePlaybackStatus getCurrentStatus(boolean forceUpdate) {
        return currentStatus.get(forceUpdate);
    }

    public void setApplicationForeground() {
        isApplicationForeground = true;

        statusCheckingPolicy.init();
    }

    public void setApplicationBackground() {
        isApplicationForeground = false;

        statusCheckingPolicy.stop();
    }

    public boolean isApplicationForeground() {
        return isApplicationForeground;
    }

    public boolean canPlayInBackground() {
        return settings.getBoolean("canPlayInBackground", true);
    }

    public void setPlayInBackground(boolean canPlayInBackground) {
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("canPlayInBackground", canPlayInBackground);
        settingsEditor.apply();
    }

    public boolean canSendNotifications() {
        return settings.getBoolean("canSendNotifications", true);
    }

    public void setSendNotifications(boolean canSendNotifications) {
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putBoolean("canSendNotifications", canSendNotifications);
        settingsEditor.apply();
    }
}
