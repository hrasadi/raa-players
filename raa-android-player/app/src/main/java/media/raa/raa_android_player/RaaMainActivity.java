package media.raa.raa_android_player;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.GoogleApiAvailability;

import java.lang.reflect.Field;
import java.util.Locale;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.view.feed.FeedLoadingFragment;
import media.raa.raa_android_player.view.livebroadcast.LiveBroadcastLoadingFragment;
import media.raa.raa_android_player.view.player.InAppPlayerControlsView;
import media.raa.raa_android_player.view.settings.SettingsFragment;

public class RaaMainActivity extends AppCompatActivity {
    BottomNavigationView navigationView;

    LiveBroadcastLoadingFragment liveBroadcastLoadingFragment;
    FeedLoadingFragment feedLoadingFragment;
    SettingsFragment settingsFragment;

    InAppPlayerControlsView playerView;

    DummyFragment dummyFragment = new DummyFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        feedLoadingFragment = FeedLoadingFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        playerView = findViewById(R.id.player);
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
                displayDummyFragment();
                //displayLiveBroadcastFragment();
                return true;
            case R.id.navigation_settings:
                displayDummyFragment();
                //displaySettingsFragment();
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
        feedLoadingFragment = FeedLoadingFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        // Default tab is the lineup when we enter foreground
        navigationView.setSelectedItemId(R.id.navigation_feed);

        // set app status for foreground
        RaaContext.getInstance().setApplicationForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();

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

    // todo
    private void displayDummyFragment() {
        this.getSupportActionBar().setTitle("");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, dummyFragment).commit();
    }

    public static class BottomNavigationViewHelper {
        @SuppressLint("RestrictedApi")
        static void removeShiftMode(BottomNavigationView view) {
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
