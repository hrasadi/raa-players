package media.raa.raa_android_player;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import media.raa.raa_android_player.model.PlaybackService;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.livebroadcast.RemotePlaybackStatus;
import media.raa.raa_android_player.view.feed.FeedLoadingFragment;
import media.raa.raa_android_player.view.livebroadcast.LiveBroadcastLoadingFragment;
import media.raa.raa_android_player.view.settings.SettingsFragment;

import static media.raa.raa_android_player.model.PlaybackService.ACTION_PLAY;
import static media.raa.raa_android_player.model.PlaybackService.ACTION_STOP;

public class RaaMainActivity extends AppCompatActivity implements SettingsFragment.OnSettingsFragmentInteractionListener {

    public static final String PLAYER_BAR_EVENT = "player_bar_event";

    BottomNavigationView navigationView;
    ImageButton playerBarActionButton;
    boolean playerBarActionButtonPlaying = true;

    MetadataUpdateEventReceiver metadataUpdateEventReceiver;
    Timer playerBarTimer;

    LiveBroadcastLoadingFragment liveBroadcastLoadingFragment;
    FeedLoadingFragment feedLoadingFragment;
    SettingsFragment settingsFragment;


    private ImageButton.OnClickListener mOnPlayerActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (playerBarActionButtonPlaying) {
                playerBarActionButtonPlaying = false;
                playerBarActionButton.setImageResource(R.drawable.ic_play_black_24dp);
                Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
                intent.setAction(ACTION_STOP);
                startService(intent);
            } else {
                playerBarActionButtonPlaying = true;
                playerBarActionButton.setImageResource(R.drawable.ic_pause_black_24dp);
                Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
                intent.setAction(ACTION_PLAY);
                startService(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI receiver for metadata update events
//        metadataUpdateEventReceiver = new MetadataUpdateEventReceiver();

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);

        // Set application language
        Locale locale = new Locale("fa_IR");
        this.getResources().getConfiguration().setLocale(locale);

        setContentView(R.layout.activity_raa_main);

        navigationView = findViewById(R.id.navigation);
        // Fit more than 3 items in navigation bar
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        liveBroadcastLoadingFragment = LiveBroadcastLoadingFragment.newInstance();
        feedLoadingFragment = feedLoadingFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        navigationView.setSelectedItemId(R.id.navigation_settings);

//        playerBarActionButton = findViewById(R.id.player_bar_action_button);
//        playerBarActionButton.setOnClickListener(mOnPlayerActionListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_live:
                displayLiveBroadcastFragment();
                return true;
            case R.id.navigation_feed:
                displayFeedFragment();
                return true;
            case R.id.navigation_archive:
                displayLiveBroadcastFragment();
                return true;
            case R.id.navigation_settings:
                displaySettingsFragment();
                return true;
        }
        return false;
    };

    @Override
    protected void onResume() {
        super.onResume();

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);

        // Create a new instance each time application comes to foreground
        liveBroadcastLoadingFragment = LiveBroadcastLoadingFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        // Default tab is the lineup when we enter foreground
        navigationView.setSelectedItemId(R.id.navigation_live);

        // start playback
        Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        startService(intent);

        // start the player bar
        startPlayerBar();

        // set app status for foreground
        RaaContext.getInstance().setApplicationForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // If system notifications are not allowed, we cannot show the service controls,
        // therefore playback will be stopped upon quit
        // Also if user changed the settings to prevent background play
        if (!NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled()
            || !RaaContext.getInstance().canPlayInBackground()) {
            // If user does not allow notification, stop the service
            Intent intent = new Intent(getApplicationContext(), PlaybackService.class);
            intent.setAction(ACTION_STOP);
            // Stop the playback
            startService(intent);
        }

        // Stop the player bar
        stopPlayerBar();

        // set app status for background
        RaaContext.getInstance().setApplicationBackground();
    }

    private void displayLiveBroadcastFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_live));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, liveBroadcastLoadingFragment).commit();
    }

    private void displayFeedFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_feed));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, feedLoadingFragment).commit();
    }

    private void displaySettingsFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_settings));
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, settingsFragment).commit();
    }

    @Override
    public void onCanPlaybackInBackgroundChange(boolean newValue) {
        RaaContext.getInstance().setPlayInBackground(newValue);
    }

    @Override
    public void onCanSendNotificationsChange(boolean newValue) {
        RaaContext.getInstance().setSendNotifications(newValue);
    }

    private class MetadataUpdateEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // OK. Something has changed. Let's figure our what to do!
            // first let's update the bar title
            final RemotePlaybackStatus playbackStatus = RaaContext.getInstance().getCurrentStatus(false);
            if (!playbackStatus.isCurrentlyPlaying()) {
                if (playbackStatus.getNextBoxId() == null) {
                    // No more programs for today
                    ((TextView) findViewById(R.id.player_bar_program_title))
                            .setText(R.string.status_program_finish);
                } else {
                    long counterInMs = playbackStatus.getNextBoxStartTime().getTime() - new Date().getTime();
                    final long counterInSec = TimeUnit.MILLISECONDS.toSeconds(counterInMs);

                    // Show the countdown (update every one second)
                    playerBarTimer.schedule(new TimerTask() {

                        private long counter = counterInSec;

                        @Override
                        public void run() {
                            String timeRemainingString = "";

                            if (counter > 0) {
                                counter--;

                                if (counter / 3600 != 0) {
                                    timeRemainingString = timeRemainingString + counter / 3600 + " ساعت و ";
                                }
                                long remaining = counter % 3600;
                                if (remaining / 60 != 0) {
                                    timeRemainingString = timeRemainingString + remaining / 60 + " دقیقه و ";
                                }
                                remaining = remaining % 60;
                                timeRemainingString = timeRemainingString + remaining + " ثانیه ";

                                timeRemainingString = Utils.convertToPersianLocaleString(timeRemainingString);
                                timeRemainingString = String.format("%s در %s", playbackStatus.getNextBoxId(), timeRemainingString);

                            } else {
                                timeRemainingString = String.format("به زودی: %s", playbackStatus.getNextBoxId());
                                this.cancel();
                            }

                            final String counterString = timeRemainingString;
                            RaaMainActivity.this.runOnUiThread(() -> ((TextView) findViewById(R.id.player_bar_program_title))
                                    .setText(counterString));
                        }
                    }, 0, 1000);
                }

            } else {
                // cancel any previous timers
                playerBarTimer.cancel();

                // Now playing + current program name
                ((TextView) findViewById(R.id.player_bar_program_title))
                        .setText(String.format(getResources().getString(R.string.status_now_playing),
                                playbackStatus.getCurrentProgram()));
            }
        }
    }

    private void startPlayerBar() {
        // Create a new timer
        playerBarTimer = new Timer("PlayerBarTimer");

        // Also register the listener for the playback status bar
        LocalBroadcastManager.getInstance(this).registerReceiver(metadataUpdateEventReceiver,
                new IntentFilter(PLAYER_BAR_EVENT)
        );
    }

    private void stopPlayerBar() {
        // Stop any timers
        playerBarTimer.purge();
        playerBarTimer.cancel();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(metadataUpdateEventReceiver);
    }

    public static class BottomNavigationViewHelper {
        @SuppressLint("RestrictedApi")
        public static void removeShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BottomNav", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BottomNav", "Unable to change value of shift mode", e);
            }
        }
    }
}
