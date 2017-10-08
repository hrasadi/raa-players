package media.raa.raa_android_player.model.lineup;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.OffsetDateTime;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * This class represents the current status of the playback from server.
 * Values shall be obtained from both reading the status.json from server and from push
 * notifications broadcast to clients
 * Created by hamid on 10/6/17.
 */

public class RemotePlaybackStatus {

    private boolean isCurrentlyPlaying;
    private String currentBox;
    private String currentProgram;
    private String currentClip;

    private String nextBoxId;
    private Date nextBoxStartTime;

    public RemotePlaybackStatus() {
        reloadStatus();
    }

    public RemotePlaybackStatus get(boolean forceUpdate) {
        if (forceUpdate) {
            reloadStatus();
        }
        return this;
    }

    private void reloadStatus() {
        String statusString = readServerStatus();
        parseServerStatus(statusString);
    }

    public boolean isCurrentlyPlaying() {
        return isCurrentlyPlaying;
    }

    public String getCurrentBox() {
        return currentBox;
    }

    public String getCurrentProgram() {
        return currentProgram;
    }

    public String getCurrentClip() {
        return currentClip;
    }

    public String getNextBoxId() {
        return nextBoxId;
    }

    public Date getNextBoxStartTime() {
        return nextBoxStartTime;
    }

    private String readServerStatus() {
        String serverResponse = null;

        String statusUrlString = "https://raa.media/lineups/status.json";

        try {
            URL url = new URL(statusUrlString);
            URLConnection urlConnection = url.openConnection();
            InputStream inputStream = urlConnection.getInputStream();

            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line).append("\n");
            }

            inputStream.close();
            serverResponse = sBuilder.toString();

        } catch (Exception e) {
            Log.e("Raa", "Error converting result " + e.toString());
        }

        return serverResponse;
    }

    private void parseServerStatus(String serverResponse) {
        try {
            if (serverResponse != null) {
                JSONObject status = new JSONObject(serverResponse);
                isCurrentlyPlaying = status.getBoolean("isCurrentlyPlaying");
                if (isCurrentlyPlaying) {
                    currentBox = status.getString("currentBox");
                    currentProgram = status.getString("currentProgram");
                    currentClip = status.getString("currentClip");
                } else {
                    nextBoxId = status.getString("nextBoxId");
                    // OMG! How hard this conversion is in Java
                    nextBoxStartTime = DateTimeUtils.toDate(OffsetDateTime
                            .parse(status.getString("nextBoxStartTime")).toInstant());
                    currentProgram = null;
                    currentClip = null;
                }
            }
        } catch (JSONException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }


    // Methods: 1- receive push notification
    // 2- read server status
}
