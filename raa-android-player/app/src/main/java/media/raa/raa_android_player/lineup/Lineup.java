package media.raa.raa_android_player.lineup;

import android.os.AsyncTask;
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

public class Lineup {

    private List<Program> currentLineup;

    public Lineup() {
        this.reloadLineup();
    }

    public void reloadLineup() {
        currentLineup = new ArrayList<>();
        LineupLoader loader = new LineupLoader();
        loader.execute();
    }

    public List<Program> getCurrentLineup() {
        return currentLineup;
    }

    private class LineupLoader extends AsyncTask<String, String, Void> {
        String result;

        @Override
        protected Void doInBackground(String... params) {

            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            String lineupFileUrlString = "https://raa.media/lineups/lineup-" +
                    dateFormatter.format(new Date()) + ".json";

            try {
                URL url = new URL(lineupFileUrlString);
                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 8);
                StringBuilder sBuilder = new StringBuilder();

                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sBuilder.append(line).append("\n");
                }

                inputStream.close();
                result = sBuilder.toString();

            } catch (Exception e) {
                Log.e("Raa", "Error converting result " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            //parse JSON data
            try {
                JSONArray programsArray = new JSONObject(result).getJSONArray("array");
                for (int i = 0; i < programsArray.length(); i++) {
                    JSONObject programJSON = programsArray.getJSONObject(i);

                    String title = programJSON.getString("title");
                    String description = Html.fromHtml(programJSON.getString("description")).toString();
                    String startTime = programJSON.getString("startTime");
                    String endTime = programJSON.getString("endTime");

                    currentLineup.add(new Program(startTime + " - " + endTime, title, description));
                } // End Loop
            } catch (JSONException e) {
                Log.e("Raa", "Error: " + e.toString());
            }
        }
    }
}
