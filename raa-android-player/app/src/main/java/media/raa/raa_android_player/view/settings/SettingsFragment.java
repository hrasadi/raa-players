package media.raa.raa_android_player.view.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 */
public class SettingsFragment extends Fragment {

    private OnSettingsFragmentInteractionListener mListener;

    private Switch canPlayInBackgroundSwitch;
    private Switch canSendNotificationsSwitch;

    public SettingsFragment() {

    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        setupSettingsSwitches(view);

        return view;
    }

    private void setupSettingsSwitches(View view) {
        canPlayInBackgroundSwitch = view.findViewById(R.id.can_play_in_background_switch);
        canPlayInBackgroundSwitch.setChecked(RaaContext.getInstance().canPlayInBackground());
        canPlayInBackgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mListener.onCanPlaybackInBackgroundChange(b);
            }
        });

        canSendNotificationsSwitch = view.findViewById(R.id.can_send_nitifications_switch);
        canSendNotificationsSwitch.setChecked(RaaContext.getInstance().canSendNotifications());
        canSendNotificationsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mListener.onCanSendNotificationsChange(b);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsFragmentInteractionListener) {
            mListener = (OnSettingsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSettingsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnSettingsFragmentInteractionListener {
        void onCanPlaybackInBackgroundChange(boolean newValue);
        void onCanSendNotificationsChange(boolean newValue);
    }
}
