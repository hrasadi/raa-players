package media.raa.raa_android_player.model.entities.livebroadcast;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.Promise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import media.raa.raa_android_player.model.JSONReader;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;

@SuppressWarnings("unused")
public class LiveBroadcastLineup extends JSONReader {
    private static final String LIVE_BROADCAST_PREFIX_URL = RaaContext.BASE_URL + "/live";
    private static final String LIVE_BROADCAST_LINEUP_URL = LIVE_BROADCAST_PREFIX_URL + "/live-lineup.json";
    private static final String LIVE_BROADCAST_STATUS_URL = LiveBroadcastLineup.LIVE_BROADCAST_PREFIX_URL + "/status.json";

    private Map<String, List<Program>> lineup;
    private List<Program> flatLineup = new ArrayList<>();

    private LiveBroadcastStatus broadcastStatus;
    private OnBroadcastStatusUpdated onBroadcastStatusUpdated;

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public Promise reload() {
        return dm.when(() -> this.readRemoteJSON(LIVE_BROADCAST_LINEUP_URL),
                () -> this.readRemoteJSON(LIVE_BROADCAST_STATUS_URL))
                .done(result -> {
                    this.parseLiveBroadcastLineup(result.getFirst().getValue());
                    this.flattenLineup();

                    this.parseLiveBroadcastStatus(result.getSecond().getValue());
                });
    }

    public Promise reloadStatus() {
        return dm.when(() -> this.readRemoteJSON(LIVE_BROADCAST_STATUS_URL))
                .done(this::parseLiveBroadcastStatus);
    }

    private void parseLiveBroadcastLineup(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.lineup = gson.fromJson(serverResponse,
                        new TypeToken<Map<String, List<Program>>>() {}.getType());
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    private void parseLiveBroadcastStatus(String serverResponse) {
        try {
            if (serverResponse != null) {
                this.broadcastStatus = gson.fromJson(serverResponse,
                        new TypeToken<LiveBroadcastStatus>() {}.getType());
            }

            if (onBroadcastStatusUpdated != null) {
                onBroadcastStatusUpdated.perform();
            }
        } catch (JsonSyntaxException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }

    private void flattenLineup() {
        this.flatLineup = new ArrayList<>();
        List<String> dates = new ArrayList<>(this.lineup.keySet());
        Collections.sort(dates);
        for (String date: dates) {
            this.flatLineup.addAll(this.lineup.get(date));
        }
    }

    public Map<String, List<Program>> getLineup() {
        return lineup;
    }

    public List<Program> getFlatLineup() {
        return flatLineup;
    }

    public LiveBroadcastStatus getBroadcastStatus() {
        return broadcastStatus;
    }

    public int getMostRecentProgramIndex() {
        for (int i = 0; i < this.flatLineup.size(); i++) {
            if (this.flatLineup.get(i).getCanonicalIdPath().equals(this.broadcastStatus.getMostRecentProgram())) {
                return i;
            }
        }
        return 0;
    }

    public Program getMostRecentProgram() {
        return flatLineup.get(this.getMostRecentProgramIndex());
    }

    public String getNextProgramCanonicalIdPath() {
        if (this.getMostRecentProgramIndex() + 1 < this.flatLineup.size()) {
            return this.flatLineup.get(this.getMostRecentProgramIndex() + 1).getCanonicalIdPath();
        }
        return null;
    }

    public void setOnBroadcastStatusUpdated(OnBroadcastStatusUpdated onBroadcastStatusUpdated) {
        this.onBroadcastStatusUpdated = onBroadcastStatusUpdated;
    }

    // Callback for live status update (every 10 seconds + notifications)
    public interface OnBroadcastStatusUpdated {
        void perform();
    }
}
