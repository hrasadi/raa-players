package media.raa.raa_android_player.model;

import media.raa.raa_android_player.model.lineup.Lineup;
import media.raa.raa_android_player.model.lineup.RemotePlaybackStatus;

/**
 * Singleton container of Raa common
 * Created by hamid on 9/30/17.
 */

public class RaaContext {
    private static RaaContext instance;
    private static PlaybackService playbackService;

    static {
        instance = new RaaContext();
    }

    public static RaaContext getInstance() {
        return instance;
    }

    private Lineup currentLineup;
    private RemotePlaybackStatus currentStatus;

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

}
