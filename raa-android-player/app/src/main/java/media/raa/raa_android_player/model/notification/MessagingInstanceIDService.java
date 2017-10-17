package media.raa.raa_android_player.model.notification;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Messaging manager
 * Created by hamid on 10/6/17.
 */

public class MessagingInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Raa", "Token is: " + refreshedToken);

        String deviceRegistrationUrlString = "https://api.raa.media/registerDevice/fcm/" + refreshedToken;

        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(deviceRegistrationUrlString).openConnection();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Set RemotePlaybackStatus policy to notification based
                Log.d("Raa", "Successfully registered device with FCM. Use notification based status management");
            } else {
                Log.d("Raa", "Failed to register the device with server. Fallback to legacy status manager");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
