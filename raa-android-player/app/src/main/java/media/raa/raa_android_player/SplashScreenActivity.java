package media.raa.raa_android_player;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.programinfodirectory.ProgramInfoDirectory;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        JodaTimeAndroid.init(this);

        RaaContext.initializeInstance(this);
        ProgramInfoDirectory pid = RaaContext.getInstance().getProgramInfoDirectory();
        //noinspection unchecked
        pid.reload().done(rs -> {
            Handler mainHandler = new Handler(this.getMainLooper());

            mainHandler.post(() -> {
                ((TextView) findViewById(R.id.splash_progress_text_view)).setText(R.string.label_splash_work_done);
                startActivity(new Intent(SplashScreenActivity.this, RaaMainActivity.class));
            });
        });
    }
}
