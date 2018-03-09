package media.raa.raa_android_player.model.entities.programinfodirectory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.DeferredManager;
import org.jdeferred2.DoneCallback;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DefaultDeferredManager;
import org.jdeferred2.impl.DeferredObject;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;

/**
 * ProgramInfo Directory
 * Created by hamid on 3/1/18.
 */

@SuppressWarnings({"unchecked", "unused"})
public class ProgramInfoDirectory {

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private DeferredManager dm = new DefaultDeferredManager(executorService);

    private Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    private Map<String, ProgramInfo> programInfoMap;

    public Promise reload() {
        if (programInfoMap != null) {
            // Already loaded, use the cached values;
            return new DeferredObject().resolve(null);
        }
        return dm.when(this::loadProgramInfoMap).then(
                (DoneCallback<? super Void>) result -> ProgramInfoDirectory.this.preloadImages());
    }

    private void loadProgramInfoMap() {
        String serverResponse;
        try {
            URL url = new URL(RaaContext.API_PREFIX_URL + "/programInfoDirectory");
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

            JSONObject json = new JSONObject(serverResponse);
            //noinspection SpellCheckingInspection
            this.programInfoMap = gson.fromJson(json.getString("ProgramInfos"),
                    new TypeToken<HashMap<String, ProgramInfo>>(){}.getType());

        } catch (Exception e) {
            Log.e("Raa", "Error while refreshing program info map!" + e.toString());
        }
    }

    private void preloadImages() {
        for (ProgramInfo pi : this.programInfoMap.values()) {
            if (pi.getBanner() != null) {
                // download banner
                pi.setBannerBitmap(downloadBitmap(pi.getBanner()));
            }
            if (pi.getThumbnail() != null) {
                // download banner
                pi.setThumbnailBitmap(downloadBitmap(pi.getThumbnail()));
            }
        }
    }

    private Bitmap downloadBitmap(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            Bitmap result = BitmapFactory.decodeStream(input);
            input.close();

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, ProgramInfo> getProgramInfoMap() {
        return programInfoMap;
    }
}