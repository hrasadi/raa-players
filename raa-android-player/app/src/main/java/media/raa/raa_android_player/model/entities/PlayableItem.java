package media.raa.raa_android_player.model.entities;

public interface PlayableItem {
    String getMainMediaSourceUrl();
    Long getRemainingDuration();
    void restartPlayback();
    void resumePlayback();
}
