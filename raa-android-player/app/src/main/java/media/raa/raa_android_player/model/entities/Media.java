package media.raa.raa_android_player.model.entities;

/**
 * A media, represents a unique playable file
 * Created by hamid on 3/1/18.
 */

public class Media {
    private String path;
    private Double duration;

    public Media(String path, Double duration) {
        this.path = path;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Double getDuration() {
        return duration;
    }

    public void setDuration(Double duration) {
        this.duration = duration;
    }
}
