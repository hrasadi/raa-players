package media.raa.raa_android_player.model;

import android.util.Log;

import org.jdeferred2.DeferredManager;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DefaultDeferredManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Created by hamid on 3/2/18.
 */

public abstract class JSONReader {
    protected ExecutorService executorService = Executors.newCachedThreadPool();
    protected DeferredManager dm = new DefaultDeferredManager(executorService);

    public abstract Promise reload();

    protected String readServerLineup(String urlString) {
        String serverResponse = null;

        try {
            URL url = new URL(urlString);
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

}
