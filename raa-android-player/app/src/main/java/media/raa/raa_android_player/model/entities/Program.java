package media.raa.raa_android_player.model.entities;

/**
 * Represents one program in its general form (inside a feed or a lineup)
 * Created by hamid on 9/30/17.
 */

@SuppressWarnings("WeakerAccess")
public class Program {
    private String programId;
    private String title;
    private String subtitle;
    private String canonicalIdPath;

    private Show show;
    private Show preShow;

    private Metadata metadata;

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getCanonicalIdPath() {
        return canonicalIdPath;
    }

    public void setCanonicalIdPath(String canonicalIdPath) {
        this.canonicalIdPath = canonicalIdPath;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Show getPreShow() {
        return preShow;
    }

    public void setPreShow(Show preShow) {
        this.preShow = preShow;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}