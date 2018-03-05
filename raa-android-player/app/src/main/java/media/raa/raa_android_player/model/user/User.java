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

    private transient Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public User() {
    }

    public String getLocationString() {
        return (country != null ? country : "") + "/" +
                (state != null ? state : "") + "/" + (city != null ? city : "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNotificationToken() {
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

    public String getNotificationExcludedPublicProgramsString() {
        if (this.notificationExcludedPublicPrograms != null) {
            return gson.toJson(this.notificationExcludedPublicPrograms);
        }
        return null;
    }

    public void setNotificationExcludedPublicProgramsString(String notificationExcludedPublicProgramsString) {
        this.notificationExcludedPublicProgramsString = notificationExcludedPublicProgramsString;
        this.notificationExcludedPublicPrograms =
                gson.fromJson(this.notificationExcludedPublicProgramsString, new TypeToken<Map<String, Boolean>>(){}.getType());
    }

    public Map<String, Boolean> getNotificationExcludedPublicPrograms() {
        return notificationExcludedPublicPrograms;
    }

    public void setNotificationExcludedPublicPrograms(Map<String, Boolean> notificationExcludedPublicPrograms) {
        this.notificationExcludedPublicPrograms = notificationExcludedPublicPrograms;
    }
}
