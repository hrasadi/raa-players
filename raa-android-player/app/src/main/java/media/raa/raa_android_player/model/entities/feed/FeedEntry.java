package media.raa.raa_android_player.model.entities.feed;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Locale;

import media.raa.raa_android_player.Utils;
import media.raa.raa_android_player.model.entities.PlayableItem;
import media.raa.raa_android_player.model.entities.Program;

/**
 * Common data-structure for a feed
 * Created by hamid on 3/1/18.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class FeedEntry implements PlayableItem {
    private String id;
    @SerializedName("Program")
    private String programString;
    private long releaseTimestamp;
    private long expirationTimestamp;

    Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public FeedEntry() {
    }

    private String getFormattedTime(long timestamp) {
        DateTime d = new DateTime(timestamp * 1000);
        String zonedDateString = d.withZone(DateTimeZone.getDefault()).toString("HH:mm", Locale.US);

        return Utils.convertToPersianLocaleString(zonedDateString);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProgramString() {
        return programString;
    }

    public void setProgramString(String programString) {
        this.programString = programString;
    }

    public Program getProgram() {
        return gson.fromJson(programString, Program.class);
    }

    public long getReleaseTimestamp() {
        return releaseTimestamp;
    }

    public String getFormattedReleaseTime() {
        return this.getFormattedTime(this.releaseTimestamp);
    }

    public String getReleaseTimeRelativeDay() {
        return Utils.getRelativeDayName(new DateTime(this.releaseTimestamp * 1000));
    }

    public void setReleaseTimestamp(long releaseTimestamp) {
        this.releaseTimestamp = releaseTimestamp;
    }

    public long getExpirationTimestamp() {
        return expirationTimestamp;
    }

    public String getFormattedExpirationTime() {
        return this.getFormattedTime(this.expirationTimestamp);
    }

    public String getExpirationTimeRelativeDay() {
        return Utils.getRelativeDayName(new DateTime(this.expirationTimestamp * 1000));
    }

    public void setExpirationTimestamp(long expirationTimestamp) {
        this.expirationTimestamp = expirationTimestamp;
    }

    @Override
    public String getMainMediaSourceUrl() {
        return this.getProgram().getShow().getClips()[0].getMedia().getPath();
    }
}
