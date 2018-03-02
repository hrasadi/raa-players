package media.raa.raa_android_player.model.feed;

import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.feed.FeedEntry;

/**
 * Represents a public entry in the feed
 * Created by hamid on 3/1/18.
 */

public class PublicFeedEntry extends FeedEntry {
    private int upvotes;

    public PublicFeedEntry(String id, Program program, Double releaseTimestamp, Double expirationTimestamp, int upvotes) {
        super(id, program, releaseTimestamp, expirationTimestamp);
        this.upvotes = upvotes;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }
}
