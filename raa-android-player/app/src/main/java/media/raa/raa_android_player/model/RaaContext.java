package media.raa.raa_android_player.model;

import media.raa.raa_android_player.model.lineup.Lineup;

/**
 * Singleton container
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

    private RaaContext() {
        currentLineup = new Lineup();
    }

    public Lineup getLineup() {
        return currentLineup;
    }

}
