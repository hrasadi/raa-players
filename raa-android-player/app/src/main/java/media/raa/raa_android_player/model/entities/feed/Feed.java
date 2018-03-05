package media.raa.raa_android_player.model.entities.feed;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.Promise;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import media.raa.raa_android_player.model.JSONReader;
import media.raa.raa_android_player.model.RaaContext;

/**
 * Represents the feed lists (personal and public)
 * Created by hamid on 3/2/18.
 */

public class Feed extends JSONReader {

    private static final String PUBLIC_FEED_URL = RaaContext.API_PREFIX_URL + "/publicFeed";
    private static final String PERSONAL_FEED_URL_PREFIX = RaaContext.API_PREFIX_URL + "/personalFeed";

    private static final int PERSONAL_ENTRIES_FROM_FUTURE_TO_SHOW = 1;

    private List<PublicFeedEntry> publicFeed;
    private List<PersonalFeedEntry> personalFeed;

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public Promise reload() {
        String personalFeedUrl = PERSONAL_FEED_URL_PREFIX + "/" +
                RaaContext.getInstance().getUserManager().getUser().getId();

        return dm.when(() -> this.readServerLineup(PUBLIC_FEED_URL),
                () -> this.readServerLineup(personalFeedUrl))
                .done((responses) -> {
                    this.parsePublicFeed(responses.getFirst().getValue());
                    this.parsePersonalFeed(responses.getSecond().getValue());
                });
    }

    private void parsePublicFeed(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.publicFeed = gson.fromJson(serverResponse,
                        new TypeToken<List<PublicFeedEntry>>() {
                        }.getType());
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    private void parsePersonalFeed(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.personalFeed = gson.fromJson(serverResponse,
                        new TypeToken<List<PersonalFeedEntry>>() {
                        }.getType());
            }

            Collections.sort(this.personalFeed, (p1, p2) -> {
                if (p1.getReleaseTimestamp() < p2.getReleaseTimestamp()) {
                    return -1;
                } else if (p1.getReleaseTimestamp() < p2.getReleaseTimestamp()) {
                    return 1;
                }
                return 0;
            });

            // Now filter the feed
            int lastFeedIndexToShow;
            int futureEntriesIncluded = 0;
            for (lastFeedIndexToShow = 0; lastFeedIndexToShow < this.personalFeed.size(); lastFeedIndexToShow++) {
                if (personalFeed.get(lastFeedIndexToShow).getReleaseTimestamp() > DateTime.now().getMillis() / 1000) {
                    futureEntriesIncluded++;
                }
                if (futureEntriesIncluded > PERSONAL_ENTRIES_FROM_FUTURE_TO_SHOW) {
                    break;
                }
            }
            this.personalFeed = this.personalFeed.subList(0, lastFeedIndexToShow);
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    public List<PublicFeedEntry> getPublicFeed() {
        return publicFeed;
    }

    public List<PersonalFeedEntry> getPersonalFeed() {
        return personalFeed;
    }
}
