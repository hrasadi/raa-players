package media.raa.raa_android_player.view.archive;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 * Loads the archive directory list
 */
public class ArchiveListLoadingFragment extends Fragment {

    public static ArchiveListLoadingFragment newInstance() {
        return new ArchiveListLoadingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        //noinspection unchecked
        RaaContext.getInstance().getArchive().reload().done(rs -> {
            ArchiveListFragment fragment = ArchiveListFragment.newInstance();

            Handler mainHandler = new Handler(getContext().getMainLooper());
            mainHandler.post(() -> getFragmentManager().beginTransaction()
                    .replace(R.id.application_frame, fragment).commitAllowingStateLoss());

        });
        return inflater.inflate(R.layout.fragment_loading, container, false);
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

