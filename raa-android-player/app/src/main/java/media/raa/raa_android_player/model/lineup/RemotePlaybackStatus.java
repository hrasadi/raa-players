package media.raa.raa_android_player.model.lineup;

import media.raa.raa_android_player.model.PlaybackService;

/**
 * This class represents the current status of the playback from server.
 * Values shall be obtained from both reading the status.json from server and from push
 * notifications broadcast to clients
 * Created by hamid on 10/6/17.
 */

public class RemotePlaybackStatus {

    private boolean isCurrentlyPlaying;
    private Program currentProgram;

    public RemotePlaybackStatus() {
        // Todo load status once
    }

    public RemotePlaybackStatus get(boolean forceUpdate) {
        // TODO
        return this;
    }

    public Program getCurrentProgram() {
        // TODO
        currentProgram = new Program("10-12", "برنامه‌ی تستی", "تست ۱، تست ۲");
        return currentProgram;
    }

    // Methods: 1- receive push notification
    // 2- read server status
}
