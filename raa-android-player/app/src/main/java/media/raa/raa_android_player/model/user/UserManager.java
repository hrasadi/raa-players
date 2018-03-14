package media.raa.raa_android_player.model.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
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
import java.util.Collections;
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

    // This is the first time Raa is opened after installation
    // Show Guide and force register with server
    private boolean firstApplicationLaunch = false;

    private static final String SETTINGS_USER_KEY = "user";

    private static final String LOADING_STEP_GET_LOCATION = "GET_LOCATION";
    public static final String LOADING_STEP_REFRESH_NOTIFICATION_TOKEN = "REFRESH_NOTIFICATION_TOKEN";

    private ConcurrentLinkedQueue<String> loadingSteps =
            new ConcurrentLinkedQueue<>(Collections.singletonList(LOADING_STEP_GET_LOCATION));

    public UserManager(Context context, SharedPreferences settings) {
        this.context = context;
        this.settings = settings;

        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        this.geocoder = new Geocoder(context, Locale.US);
    }

    public Promise initiate() {
        if (loadingSteps.size() == 0) {
            // The manager is already loaded, do not attempt redoing everything. Move forward
            //noinspection unchecked
            return new DeferredObject().resolve(null);
        }

        this.userManagerDeferred = new DeferredObject();

        loadUser();

        requestUserLocation();
        requestNotificationToken();

        return userManagerDeferred.promise();
    }

    private void loadUser() {
        if (this.settings.contains(SETTINGS_USER_KEY)) {
            this.user = gson.fromJson(this.settings.getString(SETTINGS_USER_KEY, null), new TypeToken<User>(){}.getType());
        } else {
            // HOORAAAY :-)
            this.firstApplicationLaunch = true;

            this.user = new User();
            this.user.setId(UUID.randomUUID().toString());
            this.user.setTimeZone(DateTimeZone.getDefault().getID());
            // User needs its notification token being generated
            this.loadingSteps.add(LOADING_STEP_REFRESH_NOTIFICATION_TOKEN);
        }
    }

    private void requestNotificationToken() {
        Log.d("Raa", "Refreshing notification token will be done asynchronously in " +
                "MessagingInstanceIdService class! Check if there is an unread token");
        if (user.getNotificationToken() == null && FirebaseInstanceId.getInstance().getToken() != null) {
            user.setNotificationToken(FirebaseInstanceId.getInstance().getToken());
            notifyLoadingTaskDone(LOADING_STEP_REFRESH_NOTIFICATION_TOKEN);
        } // else MessagingInstanceIdService will update up asynchronously
    }

    // Permission is asked for in the Splash activity
    private void requestUserLocation() {
        this.previousUserLocation = user.getLocationString();

        // Location permission not allowed! Do not touch current settings (they are either null (fallback to port jeff)
        // or last known location that our best shot)
        if (ActivityCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.w("Raa", "User denied our access to its location. Proceed without touching user location");
            notifyLoadingTaskDone("GET_LOCATION");
        } else {
            final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {

                if (!(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || lm.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                    // Location services are offline. Move on!
                    Log.w("Raa", "Location services is off. Proceed with loading without " +
                            "touching user location settings.");
                    notifyLoadingTaskDone("GET_LOCATION");
                } else {
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
                            lm.removeUpdates(this);
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
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }

    public void notifyLoadingTaskDone(String stepIdentifier) {
        // We might get multiple notifications per identifier, ignore the duplicate
        if (!loadingSteps.contains(stepIdentifier)) {
            return;
        }

        boolean shouldRegister = false;
        if (stepIdentifier.equals(LOADING_STEP_GET_LOCATION)) {
            if (!previousUserLocation.equals(user.getLocationString())) {
                shouldRegister = true;
            }
        } else if (stepIdentifier.equals(LOADING_STEP_REFRESH_NOTIFICATION_TOKEN)) {
            // We only get here if there is a new token, therefore register not matter what
            shouldRegister = true;
        } else if (!this.user.getTimeZone().equals(DateTimeZone.getDefault().getID())) {
            this.user.setTimeZone(DateTimeZone.getDefault().getID());
            shouldRegister = true;
        }

        loadingSteps.remove(stepIdentifier);

        if (loadingSteps.size() == 0) {
            if (shouldRegister || this.firstApplicationLaunch) {
                this.registerUser();
            } else {
                Log.i("Raa", "Key user preferences have not changed. Don't re-register");
            }
            // All aysnc operations are done
            //noinspection unchecked
            this.userManagerDeferred.resolve(null);
        }
    }

    public void registerUser() {
        this.user.commit();

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
        this.persistUser();
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
