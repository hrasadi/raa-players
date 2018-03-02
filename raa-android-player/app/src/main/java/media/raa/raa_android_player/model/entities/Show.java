package media.raa.raa_android_player.model.entities;

/**
 * The show inside a program
 * Created by hamid on 3/1/18.
 */

public class Show {
    private Clip[] clips;

    public Show(Clip[] clips) {
        this.clips = clips;
    }

    public Clip[] getClips() {
        return clips;
    }

    public void setClips(Clip[] clips) {
        this.clips = clips;
    }
}
