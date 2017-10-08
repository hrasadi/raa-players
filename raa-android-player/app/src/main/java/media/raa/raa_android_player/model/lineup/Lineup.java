package media.raa.raa_android_player.model.lineup;

import android.text.Html;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import media.raa.raa_android_player.model.StringHelper;

public class Lineup {

    private List<Program> currentLineup;

    public Lineup() {
        reloadLineup();
    }

    public Lineup get(boolean forceUpdate) {
        if (forceUpdate) {
            reloadLineup();
        }
        return this;
    }

    private void reloadLineup() {
        currentLineup = new ArrayList<>();

        String serverResponse = readServerLineup();
        parseServerLineup(serverResponse);
    }

    public List<Program> getCurrentLineup() {
        return currentLineup;
    }

    private String readServerLineup() {
        String serverResponse = null;

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        String lineupFileUrlString = "https://raa.media/lineups/lineup-" +
                dateFormatter.format(new Date()) + ".json";

        try {
            URL url = new URL(lineupFileUrlString);
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

    private void parseServerLineup(String serverResponse) {
        try {
            if (serverResponse != null) {
                JSONArray programsArray = new JSONObject(serverResponse).getJSONArray("array");
                for (int i = 0; i < programsArray.length(); i++) {
                    JSONObject programJSON = programsArray.getJSONObject(i);

                    String title = programJSON.getString("title");
                    @SuppressWarnings("deprecation")
                    String description = Html.fromHtml(programJSON.getString("description")).toString();
                    String startTime = StringHelper.convertToPersianLocaleString(programJSON.getString("startTime"));
                    String endTime = StringHelper.convertToPersianLocaleString(programJSON.getString("endTime"));

                    currentLineup.add(new Program(endTime + " - " + startTime, title, description));
                }
            }
        } catch (JSONException e) {
            Log.e("Raa", "Error: " + e.toString());
        }
    }
}
