package media.raa.raa_android_player.model.entities.feed;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.Promise;

import java.util.List;

import media.raa.raa_android_player.model.JSONReader;
import media.raa.raa_android_player.model.RaaContext;

/**
 * Represents the feed lists (personal and public)
 * Created by hamid on 3/2/18.
 */

public class Feed extends JSONReader {

    private static final String PUBLIC_FEED_URL = RaaContext.API_PREFIX_URL + "/publicFeed";

    private List<PublicFeedEntry> publicFeed;

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public Promise reload() {
        return dm.when(() -> this.readServerLineup(PUBLIC_FEED_URL))
                .done(this::parsePublicFeed);
    }

    private void parsePublicFeed(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.publicFeed = gson.fromJson(serverResponse,
                        new TypeToken<List<PublicFeedEntry>>() {}.getType());
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    public List<PublicFeedEntry> getPublicFeed() {
        return publicFeed;
    }
}
