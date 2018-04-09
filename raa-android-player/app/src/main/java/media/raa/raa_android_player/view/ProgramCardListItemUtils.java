package media.raa.raa_android_player.view;

import media.raa.raa_android_player.model.RaaContext;

public class ProgramCardListItemUtils {
    public static PlayableState determineCardPlayableState(String cardMediaSourceUrl) {
        if (RaaContext.getInstance().getPlaybackManager().getCurrentPlayerStatus().isPlaying() &&
                RaaContext.getInstance().getPlaybackManager()
                        .getCurrentPlayerStatus().getMediaSourceUrl().equals(
                        cardMediaSourceUrl)) {
            return PlayableState.CURRENTLY_PLAYING;
        } else {
            return PlayableState.PLAYABLE;
        }
    }

    public enum PlayableState {
        CURRENTLY_PLAYING,
        PLAYABLE
    }
}
