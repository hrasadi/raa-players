package media.raa.raa_android_player.model.entities.feed;

import android.util.Log;

import media.raa.raa_android_player.model.RaaContext;

/**
 * Represents a public entry in the feed
 * Created by hamid on 3/1/18.
 */

@SuppressWarnings("unused")
public class PublicFeedEntry extends FeedEntry {
    private int upvotes;

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    @Override
    public Long getRemainingDuration() {
        double duration =  this.getProgram().getShow().getClips()[0].getMedia().getDuration() * 1000;
        long offset = RaaContext.getInstance().getPlaybackManager().getLastPlaybackState(this.getMainMediaSourceUrl());
        return (long) duration - offset;
    }

    @Override
    public void resumePlayback() {
        long offset = RaaContext.getInstance().getPlaybackManager().getLastPlaybackState(this.getMainMediaSourceUrl());

        Log.i("Raa", "Playback resume requested for public entry: " + this.getProgram().getCanonicalIdPath());
        RaaContext.getInstance().getPlaybackManager().playPublicFeedEntry(this, offset);
    }

    @Override
    public void restartPlayback() {
        Log.i("Raa", "Playback requested for public entry: " + this.getProgram().getCanonicalIdPath());
        RaaContext.getInstance().getPlaybackManager().playPublicFeedEntry(this);
    }
}
