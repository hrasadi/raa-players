package media.raa.raa_android_player;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Locale;

import media.raa.raa_android_player.lineup.Program;

public class Player extends AppCompatActivity implements LineupFragment.OnListFragmentInteractionListener {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            RaaContext.getInstance().getLineup();
            switch (item.getItemId()) {
                case R.id.navigation_lineup:
                    LineupFragment fragment = LineupFragment.newInstance();
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.application_frame, fragment).commit(); //
                    return true;
                case R.id.navigation_settings:
                    return true;
                case R.id.navigation_podcast:
                    return true;
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void onListFragmentInteraction(Program item) {

    }
}
