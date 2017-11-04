package media.raa.raa_android_player.view.lineup;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 */
public class LineupContainerFragment extends Fragment {

    private boolean lineupLoaded = false;

    public static LineupContainerFragment newInstance() {
        return new LineupContainerFragment();
    }

    public LineupContainerFragment() {
        AsyncTask.execute(() -> {
            // Force the lineup to update
            RaaContext.getInstance().getCurrentLineup(true);

            if (getFragmentManager() != null) { // It is already shown. Update
                // OK. the lineup is loaded, lets replace everything!
                // View already loaded, lets replace
                ProgramListFragment fragment = new ProgramListFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.application_frame, fragment).commitAllowingStateLoss();
            }
            lineupLoaded = true;
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        if (lineupLoaded) {
            // OK. the lineup is loaded, lets replace everything!
            // View already loaded, lets replace
            ProgramListFragment fragment = new ProgramListFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.application_frame, fragment).commit();
            return inflater.inflate(R.layout.fragment_lineup_container, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_lineup_container, container, false);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
