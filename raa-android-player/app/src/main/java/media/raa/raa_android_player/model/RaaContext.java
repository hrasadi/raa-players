package media.raa.raa_android_player.model;

import android.content.Intent;

import media.raa.raa_android_player.model.lineup.Lineup;
import media.raa.raa_android_player.view.Player;

/**
 * Singleton container
 * Created by hamid on 9/30/17.
 */

public class RaaContext {
    private static RaaContext instance;
    private static PlaybackService playbackService;

    static {
        instance = new RaaContext();
        playbackService = new PlaybackService();
//        playbackService.startService();
    }

    public static RaaContext getInstance() {
        return instance;
    }

    private Lineup currentLineup;
    private PlaybackManager playbackManager;

    private RaaContext() {
        currentLineup = new Lineup();
        playbackManager = new PlaybackManager();
    }

    public Lineup getLineup() {
        return currentLineup;
    }

    public PlaybackManager getPlaybackManager() {
        return playbackManager;
    }
}
