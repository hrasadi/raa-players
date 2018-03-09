package media.raa.raa_android_player;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import net.danlew.android.joda.JodaTimeAndroid;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.programinfodirectory.ProgramInfoDirectory;
import media.raa.raa_android_player.model.user.UserManager;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);

        setContentView(R.layout.activity_splash_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        JodaTimeAndroid.init(this);

        RaaContext.initializeInstance(this);

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            this.proceedLoadingAfterPermissionRequest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.proceedLoadingAfterPermissionRequest();
    }

    void proceedLoadingAfterPermissionRequest() {
        final Handler mainHandler = new Handler(this.getMainLooper());

        ProgramInfoDirectory pid = RaaContext.getInstance().getProgramInfoDirectory();
        //noinspection unchecked
        pid.reload().then(result -> {
            UserManager um = RaaContext.getInstance().getUserManager();
            mainHandler.post(() -> {
                ((TextView) findViewById(R.id.splash_progress_text_view)).setText(R.string.label_splash_work_done);
                //noinspection unchecked
                um.initiate().done(rs -> startActivity(new Intent(SplashScreenActivity.this, RaaMainActivity.class)));
            });
        });

    }
}
