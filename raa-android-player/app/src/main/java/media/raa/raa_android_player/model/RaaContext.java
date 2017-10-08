package media.raa.raa_android_player.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import media.raa.raa_android_player.model.lineup.Lineup;
import media.raa.raa_android_player.model.lineup.RemotePlaybackStatus;

/**
 * Singleton container of Raa common
 * Created by hamid on 9/30/17.
 */

public class RaaContext {
    private static RaaContext instance;
    private static PlaybackService playbackService;

    public static void initializeInstance(Activity appContext) {
        instance = new RaaContext();
        instance.settings = appContext.getPreferences(Context.MODE_PRIVATE);
    }

    public static RaaContext getInstance() {
        if (instance == null) {
            throw new RuntimeException("RaaContext must be initialized before being used, consider" +
                    " calling RaaContent.initializeInstance() in your Activity.Create method.");
        }
        return instance;
    }

    private SharedPreferences settings;
    private Lineup currentLineup;
    private RemotePlaybackStatus currentStatus;

    private boolean isApplicationForeground = true;

    private RaaContext() {

    }

    /**
     * Returns the current lineup or instantiate and load data into it if an instance does not exist
     * @param forceUpdate true if the lineup should be reloaded, false if the old values are good
     * @return The lineup instance (may not be populated)
     */
    public Lineup getCurrentLineup(boolean forceUpdate) {
        if (currentLineup == null) {
            currentLineup = new Lineup();
            return currentLineup;
        } else {
            return currentLineup.get(forceUpdate);
        }
    }

    public RemotePlaybackStatus getCurrentStatus(boolean forceUpdate) {
        if (currentStatus == null) {
            currentStatus = new RemotePlaybackStatus();
            return currentStatus;
        } else {
            return currentStatus.get(forceUpdate);
        }
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
