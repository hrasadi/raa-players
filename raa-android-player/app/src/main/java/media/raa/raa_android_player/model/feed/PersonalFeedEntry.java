package media.raa.raa_android_player.model.feed;

import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.feed.FeedEntry;

/**
 * Represents a personal entry in feed
 * Created by hamid on 3/1/18.
 */

public class PersonalFeedEntry extends FeedEntry {
    public PersonalFeedEntry(String id, Program program, Double releaseTimestamp, Double expirationTimestamp) {
        super(id, program, releaseTimestamp, expirationTimestamp);
    }
}
