package media.raa.raa_android_player.view;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.RaaMainActivity;
import media.raa.raa_android_player.Utils;

/**
 */
public class PlaybackModeRequesterPopup extends DialogFragment {

    public static PlaybackModeRequesterPopup newInstance(Long remainingDuration) {
        Bundle bundle = new Bundle();
        // Backward compatibility with old items
        if (remainingDuration != null) {
            bundle.putLong("remainingDuration", remainingDuration);
        }

        PlaybackModeRequesterPopup fragment = new PlaybackModeRequesterPopup();
        fragment.setArguments(bundle);

        return fragment;
    }

    public PlaybackModeRequesterPopup() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playback_mode_requester_popup, container, false);
        TextView resumePlaybackButton = view.findViewById(R.id.playback_mode_resume_btn);

        if (getArguments().containsKey("remainingDuration")) {
            String remainingDurationString = Utils.convertToPersianLocaleString(Long.toString(getArguments().getLong("remainingDuration") / 60000));
            resumePlaybackButton.setText(getString(R.string.playback_mode_resume, remainingDurationString));
        }

        resumePlaybackButton.setOnClickListener((sender) -> {
            try {
                RaaMainActivity mainActivity = (RaaMainActivity) getActivity();
                if (mainActivity.getCurrentPlaybackModeRequesterCallback() != null) {
                    mainActivity.getCurrentPlaybackModeRequesterCallback().onResumePlaybackRequested();
                }
            } catch (ClassCastException e) {
               // Nothing
            } finally {
                dismiss();
            }
        });

        TextView fromStartPlaybackButton = view.findViewById(R.id.playback_mode_from_start_btn);
        fromStartPlaybackButton.setOnClickListener((sender) -> {
            try {
                RaaMainActivity mainActivity = (RaaMainActivity) getActivity();
                if (mainActivity.getCurrentPlaybackModeRequesterCallback() != null) {
                    mainActivity.getCurrentPlaybackModeRequesterCallback().OnRestartPlaybackRequested();
                }
            } catch (ClassCastException e) {
                // Nothing
            } finally {
                dismiss();
            }
        });

        return view;
    }

    public interface PlaybackModeRequesterCallback {
        void onResumePlaybackRequested();
        void OnRestartPlaybackRequested();
    }
}
