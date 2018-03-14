package media.raa.raa_android_player.view.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 */
public class SettingsFragment extends Fragment {

    private Switch notifyOnPersonalProgramsSwitch;
    private Button notifyOnPublicProgramsBtn;

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
        notifyOnPersonalProgramsSwitch = view.findViewById(R.id.notify_on_personal_programs_switch);
        notifyOnPublicProgramsBtn = view.findViewById(R.id.notify_on_public_programs_btn);

        notifyOnPersonalProgramsSwitch.setChecked(RaaContext.getInstance().getUserManager()
                .getUser().getNotifyOnPersonalProgram() == 1);

        notifyOnPersonalProgramsSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

        });

        // Link to public programs settings page
        notifyOnPublicProgramsBtn.setOnClickListener(sender -> {
            PublicProgramNotificationSettingsListFragment fragment =
                    PublicProgramNotificationSettingsListFragment.newInstance();

            getFragmentManager().beginTransaction()
                    .addToBackStack("publicProgramNotification")
                    .replace(R.id.application_frame, fragment)
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_settings);
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
