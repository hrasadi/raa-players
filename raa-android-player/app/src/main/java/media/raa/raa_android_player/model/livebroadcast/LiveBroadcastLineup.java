package media.raa.raa_android_player.model.livebroadcast;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.DeferredManager;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DefaultDeferredManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;

@SuppressWarnings("unused")
public class LiveBroadcastLineup {

    private static final String LIVE_BROADCAST_PREFIX_URL = RaaContext.BASE_URL + "/live";
    private static final String LIVE_BROADCAST_LINEUP_URL = LIVE_BROADCAST_PREFIX_URL + "/live-lineup.json";
    private static final String LIVE_BROADCAST_STATUS_URL = LIVE_BROADCAST_PREFIX_URL + "/status.json";


    private ExecutorService executorService = Executors.newCachedThreadPool();
    private DeferredManager dm = new DefaultDeferredManager(executorService);

    private Map<String, List<Program>> lineup;
    private List<Program> flatLineup = new ArrayList<>();

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public Promise reload() {
        return dm.when(this::readServerLineup)
                .done(result -> {
                    this.parseLiveBroadcastLineup(result);
                    this.flattenLineup();
                });
    }

    private String readServerLineup() {
        String serverResponse = null;

        try {
            URL url = new URL(LIVE_BROADCAST_LINEUP_URL);
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
}
