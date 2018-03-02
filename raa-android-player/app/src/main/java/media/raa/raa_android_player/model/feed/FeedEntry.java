package media.raa.raa_android_player.model.feed;

import media.raa.raa_android_player.model.entities.Program;

/**
 * Common data-structure for a feed
 * Created by hamid on 3/1/18.
 */

public abstract class FeedEntry {
    private String id;
    private Program program;
    private Double releaseTimestamp;
    private Double expirationTimestamp;

    public FeedEntry(String id, Program program, Double releaseTimestamp, Double expirationTimestamp) {
        this.id = id;
        this.program = program;
        this.releaseTimestamp = releaseTimestamp;
        this.expirationTimestamp = expirationTimestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public Double getReleaseTimestamp() {
        return releaseTimestamp;
    }

    public void setReleaseTimestamp(Double releaseTimestamp) {
        this.releaseTimestamp = releaseTimestamp;
    }

    public Double getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public void setExpirationTimestamp(Double expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }
}
