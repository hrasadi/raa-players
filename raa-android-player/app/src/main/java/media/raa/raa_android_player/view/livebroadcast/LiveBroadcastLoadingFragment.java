package media.raa.raa_android_player.view.livebroadcast;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 */
public class LiveBroadcastLoadingFragment extends Fragment {

    public static LiveBroadcastLoadingFragment newInstance() {
        return new LiveBroadcastLoadingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        //noinspection unchecked
        RaaContext.getInstance().getLiveBroadcastLineup().reload().done(rs -> {
            LiveBroadcastListViewFragment fragment = LiveBroadcastListViewFragment.newInstance();

            Handler mainHandler = new Handler(getContext().getMainLooper());
            mainHandler.post(() -> {
                getFragmentManager().beginTransaction()
                        .replace(R.id.application_frame, fragment).commitAllowingStateLoss();

                RaaContext.getInstance().getLiveBroadcastLineup().setOnBroadcastStatusUpdated(() -> {
                    // Refresh the fragment
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.detach(fragment);
                    ft.attach(fragment);
                    ft.commit();
                });

            });

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
