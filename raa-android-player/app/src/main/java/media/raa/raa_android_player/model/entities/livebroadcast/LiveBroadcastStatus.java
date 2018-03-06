package media.raa.raa_android_player.model.entities.livebroadcast;

/**
 * This class represents the current status of the playback from server.
 * Values shall be obtained from both reading the status.json from server and from push
 * notifications broadcast to clients
 * Created by hamid on 10/6/17.
 */

@SuppressWarnings("unused")
public class LiveBroadcastStatus {
    private boolean isCurrentlyPlaying;
    private String mostRecentProgram;
    private String startedProgramTitle;

    public boolean isCurrentlyPlaying() {
        return isCurrentlyPlaying;
    }

    public String getMostRecentProgram() {
        return mostRecentProgram;
    }
}
