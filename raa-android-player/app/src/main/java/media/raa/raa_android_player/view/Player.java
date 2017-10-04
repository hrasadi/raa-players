package media.raa.raa_android_player.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Locale;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.view.lineup.LineupFragment;
import media.raa.raa_android_player.model.lineup.Program;
import media.raa.raa_android_player.view.settings.SettingsFragment;

public class Player extends AppCompatActivity implements LineupFragment.OnListFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {

    BottomNavigationView navigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_lineup:
                    displayLineupFragment();
                    return true;
                case R.id.navigation_settings:
                    displaySettingsFragment();
                    return true;
                case R.id.navigation_podcast:
                    // We do not stay here! go back to lineup
                    openPodcast();
                    return false;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set application language
        Locale locale = new Locale("fa_IR");
        this.getResources().getConfiguration().setLocale(locale);

        setContentView(R.layout.activity_player);
        // Action bar setup
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.app_title));

        //
        getApplicationContext();

        // Show the lineup by default
        navigationView = (BottomNavigationView) findViewById(R.id.navigation);
        navigationView.setSelectedItemId(R.id.navigation_lineup);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        displayLineupFragment();

        RaaContext.getInstance().getPlaybackManager().play();
    }

    private void displayLineupFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_lineup));
        LineupFragment fragment = LineupFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, fragment).commit();
    }

    private void openPodcast() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://playmusic.app.goo.gl/?ibi=com.google.PlayMusic&isi=691797987&ius=googleplaymusic&link=https://play.google.com/music/m/Iv66xcc6zt2drymno7tvkvj4kbq?t%3D%25D8%25B1%25D8%25A7%25D8%25AF%25DB%258C%25D9%2588_%25D8%25A7%25D8%25AA%25D9%2588-%25D8%25A7%25D8%25B3%25D8%25B9%25D8%25AF%26pcampaignid%3DMKT-na-all-co-pr-mu-pod-16"));
        startActivity(browserIntent);
    }

    private void displaySettingsFragment() {
        //noinspection ConstantConditions
        this.getSupportActionBar().setTitle(getString(R.string.title_settings));
        SettingsFragment fragment = SettingsFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.application_frame, fragment).commit();
    }

    public void onFragmentInteraction(Uri uri) {

    }

    public void onListFragmentInteraction(Program item) {

    }
}
