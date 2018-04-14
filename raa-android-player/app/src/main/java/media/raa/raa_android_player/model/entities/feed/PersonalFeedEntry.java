package media.raa.raa_android_player.model.entities.feed;

import org.joda.time.DateTime;

/**
 * Represents a personal entry in feed
 * Created by hamid on 3/1/18.
 */

public class PersonalFeedEntry extends FeedEntry {
    public boolean isInProgress() {
        DateTime releaseTimeObject = new DateTime(this.getReleaseTimestamp() * 1000);
        DateTime expirationTimeObject = new DateTime(this.getExpirationTimestamp() * 1000);

        return releaseTimeObject.isBeforeNow() && expirationTimeObject.isAfterNow();
    }

    @Override
    public Long getRemainingDuration() {
        // Do nothing! (for now)
        return 0L;
    }

    @Override
    public void restartPlayback() {
        // Do nothing! (for now)
    }

    @Override
    public void resumePlayback() {
        // Do nothing! (for now)
    }
}
