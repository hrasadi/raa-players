package media.raa.raa_android_player.view.player;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaybackModeRequesterPopup extends Fragment {

    public PlaybackModeRequesterPopup() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playback_mode_requester_popup, container, false);
    }

}
