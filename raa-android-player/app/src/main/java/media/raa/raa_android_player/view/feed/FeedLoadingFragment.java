package media.raa.raa_android_player.view.feed;

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
 */
public class FeedLoadingFragment extends Fragment {

    public static FeedLoadingFragment newInstance() {
        return new FeedLoadingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        //noinspection unchecked
        RaaContext.getInstance().getFeed().reload().done(rs -> {
            FeedListViewFragment fragment = FeedListViewFragment.newInstance();

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
