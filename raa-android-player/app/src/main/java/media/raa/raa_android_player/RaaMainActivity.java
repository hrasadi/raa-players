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
import media.raa.raa_android_player.view.PlaybackModeRequesterPopup;
import media.raa.raa_android_player.view.archive.ArchiveListLoadingFragment;
import media.raa.raa_android_player.view.feed.FeedLoadingFragment;
import media.raa.raa_android_player.view.livebroadcast.LiveBroadcastLoadingFragment;
import media.raa.raa_android_player.view.player.InAppPlayerControlsView;
import media.raa.raa_android_player.view.settings.SettingsFragment;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class RaaMainActivity extends AppCompatActivity {
    BottomNavigationView navigationView;

    LiveBroadcastLoadingFragment liveBroadcastLoadingFragment;
    FeedLoadingFragment feedLoadingFragment;
    ArchiveListLoadingFragment archiveLoadingFragment;
    SettingsFragment settingsFragment;

    InAppPlayerControlsView playerView;

    PlaybackModeRequesterPopup.PlaybackModeRequesterCallback currentPlaybackModeRequesterCallback;

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
        archiveLoadingFragment = ArchiveListLoadingFragment.newInstance();
        settingsFragment = SettingsFragment.newInstance();

        playerView = findViewById(R.id.player);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
//            case R.id.navigation_live:
//                displayLiveBroadcastFragment();
//                return true;
            case R.id.navigation_feed:
                displayFeedFragment();
                return true;
            case R.id.navigation_archive:
                displayArchiveListFragment();
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
        feedLoadingFragment = FeedLoadingFragment.newInstance();
        archiveLoadingFragment = ArchiveListLoadingFragment.newInstance();
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

    @Override
    public void onBackPressed() {
        // Remove any loading fragments on the top of the stack
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
        getSupportFragmentManager().popBackStack("Loading", POP_BACK_STACK_INCLUSIVE);
    }

    private void displayLiveBroadcastFragment() {
        this.clearFragmentsBackStack();

        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_live));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, liveBroadcastLoadingFragment).commit();
    }

    private void displayFeedFragment() {
        this.clearFragmentsBackStack();

        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_feed));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, feedLoadingFragment).commit();
    }

    private void displayArchiveListFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_archive));

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, archiveLoadingFragment)
                .commit();

    }

    private void displaySettingsFragment() {
        this.clearFragmentsBackStack();

        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_settings));
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, settingsFragment).commit();
    }

    private void clearFragmentsBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
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

    public PlaybackModeRequesterPopup.PlaybackModeRequesterCallback getCurrentPlaybackModeRequesterCallback() {
        return currentPlaybackModeRequesterCallback;
    }

    public void setCurrentPlaybackModeRequesterCallback(PlaybackModeRequesterPopup.PlaybackModeRequesterCallback currentPlaybackModeRequesterCallback) {
        this.currentPlaybackModeRequesterCallback = currentPlaybackModeRequesterCallback;
    }
}
