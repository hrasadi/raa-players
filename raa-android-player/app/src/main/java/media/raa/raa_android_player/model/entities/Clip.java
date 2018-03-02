package media.raa.raa_android_player.model.entities;

/**
 * A clip, used in a pre-show or show
 * Created by hamid on 3/1/18.
 */

public class Clip {
    private Media media;

    public Clip(Media media) {
        this.media = media;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }
}
