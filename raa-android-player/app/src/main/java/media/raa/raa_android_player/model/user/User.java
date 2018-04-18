package media.raa.raa_android_player.model.user;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a user
 * Created by hamid on 3/4/18.
 */

@SuppressWarnings("unused")
public class User {

    private String id;
    private String timeZone;

    private String country;
    private String city;
    private String state;

    private double latitude;
    private double longitude;

    private String notificationToken;

    private int notifyOnPersonalProgram = 1;
    private int notifyOnPublicProgram = 1;
    private int notifyOnLiveProgram = 0;

    @SerializedName("NotificationExcludedPublicPrograms")
    private String notificationExcludedPublicProgramsString;
    private transient Map<String, Boolean> notificationExcludedPublicPrograms = new HashMap<>();

    @SerializedName("NotificationExcludedPersonalPrograms")
    private String notificationExcludedPersonalProgramsString;
    private transient Map<String, Boolean> notificationExcludedPersonalPrograms = new HashMap<>();

    private transient Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    User() {
    }

    void commit() {
        if (this.notificationExcludedPublicPrograms != null) {
            this.notificationExcludedPublicProgramsString = gson.toJson(this.notificationExcludedPublicPrograms);
        }
        if (this.notificationExcludedPersonalPrograms != null) {
            this.notificationExcludedPersonalProgramsString = gson.toJson(this.notificationExcludedPersonalPrograms);
        }
    }

    String getLocationString() {
        return (country != null ? country : "") + "/" +
                (state != null ? state : "") + "/" + (city != null ? city : "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String getTimeZone() {
        return timeZone;
    }

    void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCountry() {
        return country;
    }

    void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    void setState(String state) {
        this.state = state;
    }

    public double getLatitude() {
        return latitude;
    }

    void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    String getNotificationToken() {
        return notificationToken;
    }

    public void setNotificationToken(String notificationToken) {
        this.notificationToken = notificationToken;
    }

    public int getNotifyOnPersonalProgram() {
        return notifyOnPersonalProgram;
    }

    public void setNotifyOnPersonalProgram(int notifyOnPersonalProgram) {
        this.notifyOnPersonalProgram = notifyOnPersonalProgram;
    }

    public int getNotifyOnPublicProgram() {
        return notifyOnPublicProgram;
    }

    public void setNotifyOnPublicProgram(int notifyOnPublicProgram) {
        this.notifyOnPublicProgram = notifyOnPublicProgram;
    }

    public int getNotifyOnLiveProgram() {
        return notifyOnLiveProgram;
    }

    public void setNotifyOnLiveProgram(int notifyOnLiveProgram) {
        this.notifyOnLiveProgram = notifyOnLiveProgram;
    }

    public void setNotificationExcludedPublicProgramsString(String notificationExcludedPublicProgramsString) {
        this.notificationExcludedPublicProgramsString = notificationExcludedPublicProgramsString;
    }

    public Map<String, Boolean> getNotificationExcludedPublicPrograms() {
        if (notificationExcludedPublicProgramsString != null) {
            this.notificationExcludedPublicPrograms =
                    gson.fromJson(this.notificationExcludedPublicProgramsString, new TypeToken<Map<String, Boolean>>(){}.getType());
        }

        return notificationExcludedPublicPrograms;
    }

    public void setNotificationExcludedPersonalProgramsString(String notificationExcludedPersonalProgramsString) {
        this.notificationExcludedPersonalProgramsString = notificationExcludedPersonalProgramsString;
    }

    public Map<String, Boolean> getNotificationExcludedPersonalPrograms() {
        if (notificationExcludedPersonalProgramsString != null) {
            this.notificationExcludedPersonalPrograms =
                    gson.fromJson(this.notificationExcludedPersonalProgramsString, new TypeToken<Map<String, Boolean>>(){}.getType());
        }

        return notificationExcludedPersonalPrograms;
    }
}
