package media.raa.raa_android_player.model.user;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.jdeferred2.Deferred;
import org.jdeferred2.Promise;
import org.jdeferred2.impl.DeferredObject;
import org.joda.time.DateTimeZone;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import media.raa.raa_android_player.model.RaaContext;

/**
 * Manages a user profile including preferences and server registration
 * Created by hamid on 3/4/18.
 */

public class UserManager {
    private User user;
    private String previousUserLocation;

    private Context context;
    private SharedPreferences settings;
    private Gson gson;
    private Geocoder geocoder;

    private Deferred userManagerDeferred = new DeferredObject();

    private static final String SETTINGS_USER_KEY = "user";

    private ConcurrentLinkedQueue<String> loadingSteps =
            new ConcurrentLinkedQueue<>(Arrays.asList("GET_LOCATION", "AUTHORIZE_NOTIFICATIONS"));

    public UserManager(Context context, SharedPreferences settings) {
        this.context = context;
        this.settings = settings;

        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        this.geocoder = new Geocoder(context, Locale.US);
    }

    public Promise initiate() {
        this.userManagerDeferred = new DeferredObject();

        loadUser();

        this.previousUserLocation = user.getLocationString();
        requestUserLocation();
        requestSystemNotifications();

        return userManagerDeferred.promise();
    }

    private void requestSystemNotifications() {
        notifyLoadingTaskDone("AUTHORIZE_NOTIFICATIONS");
    }

    private void loadUser() {
        if (this.settings.contains(SETTINGS_USER_KEY)) {
            this.user = gson.fromJson(this.settings.getString(SETTINGS_USER_KEY, null), new TypeToken<User>(){}.getType());
        } else {
            this.user = new User();
            this.user.setId(UUID.randomUUID().toString());
            this.user.setTimeZone(DateTimeZone.getDefault().getID());
        }
    }

    // Permission is asked for in the Splash activity
    @SuppressLint("MissingPermission")
    private void requestUserLocation() {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    user.setLatitude(location.getLatitude());
                    user.setLongitude(location.getLongitude());
                    try {
                        List<Address> address = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (address.size() > 0) {
                            user.setCountry(address.get(0).getCountryCode());
                            user.setState(address.get(0).getAdminArea());
                            user.setCity(address.get(0).getLocality());
                        }
                    } catch (IOException e) {
                        Log.e("Raa", "Error while loading address from location: ", e);
                    }
                }
                if (lm != null) {
                    lm.removeUpdates(this);
                }
                // Done with this step
                notifyLoadingTaskDone("GET_LOCATION");
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        if (lm != null) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    private void notifyLoadingTaskDone(String stepIdentifier) {
        boolean shouldRegister = false;

        if (stepIdentifier.equals("GET_LOCATION")) {
            if (!previousUserLocation.equals(user.getLocationString())) {
                shouldRegister = true;
            }
        } else if (stepIdentifier.equals("GENERATE_NOTIFICATION_TOKEN")) {
            Log.d("Raa", "Not implemented!");
        }

        loadingSteps.remove(stepIdentifier);

        if (loadingSteps.size() == 0) {
            if (shouldRegister) {
                this.registerUser();
                this.persistUser();
            } else {
                Log.i("Raa", "Key user preferences have not changed. Don't re-register");
            }

            // All aysnc operations are done
            //noinspection unchecked
            this.userManagerDeferred.resolve(null);
        }
    }

    private void registerUser() {
        AsyncTask.execute(() -> {
            try {
                URL url = new URL(RaaContext.API_PREFIX_URL + "/registerDevice/Android");

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                urlConnection.setDoOutput(true);
                BufferedOutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
                outputStream.write(gson.toJson(this.user).getBytes());
                outputStream.flush();
                outputStream.close();

                urlConnection.connect();

                Log.d("Raa", Integer.toString(urlConnection.getResponseCode()));
                Log.i("Raa", "User registered successfully");
            } catch (IOException e) {
                Log.e("Raa", "Error while registering user with server.", e);
            }
        });
    }

    private void persistUser() {
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putString(SETTINGS_USER_KEY, gson.toJson(this.user));
        editor.apply();
    }

    public User getUser() {
        return user;
    }
}
