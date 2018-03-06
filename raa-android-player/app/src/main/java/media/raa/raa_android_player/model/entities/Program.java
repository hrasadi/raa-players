package media.raa.raa_android_player.model.entities;

import media.raa.raa_android_player.model.RaaContext;

/**
 * Represents one program in its general form (inside a feed or a lineup)
 * Created by hamid on 9/30/17.
 */

@SuppressWarnings("unused")
public class Program {
    private String programId;
    private String title;
    private String subtitle;
    private String canonicalIdPath;

    private Show show;
    private Show preShow;

    private Metadata metadata;

    public boolean isNextInLine() {
        return !RaaContext.getInstance().getLiveBroadcastLineup().getBroadcastStatus().isCurrentlyPlaying() &&
                RaaContext.getInstance().getLiveBroadcastLineup().getNextProgramCanonicalIdPath().equals(this.canonicalIdPath);
    }

    public String getProgramId() {
        return programId;
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

    public String getCanonicalIdPath() {
        return canonicalIdPath;
    }

    public Show getShow() {
        return show;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}