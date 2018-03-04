package media.raa.raa_android_player.model.entities.feed;

import media.raa.raa_android_player.model.entities.Program;

/**
 * Represents a public entry in the feed
 * Created by hamid on 3/1/18.
 */

public class PublicFeedEntry extends FeedEntry {
    private int upvotes;

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }
}
