package media.raa.raa_android_player.view.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.user.UserManager;

/**
 * Displays archive directory list
 */
public class PersonalProgramNotificationSettingsListFragment extends Fragment {

    private UserManager userManager = RaaContext.getInstance().getUserManager();

    public static PersonalProgramNotificationSettingsListFragment newInstance() {
        return new PersonalProgramNotificationSettingsListFragment();
    }

    public PersonalProgramNotificationSettingsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_program_notification_settings_list, container, false);

        Switch notifyOnPersonalProgramsSwitch = view.findViewById(R.id.notify_on_personal_programs_switch);
        RecyclerView recyclerView = view.findViewById(R.id.list);
        Context recyclerViewContext = recyclerView.getContext();

        // Set the parent switch
        notifyOnPersonalProgramsSwitch.setChecked(userManager.getUser().getNotifyOnPersonalProgram() == 1);
        notifyOnPersonalProgramsSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean shouldRegister = false;
            if (!b && userManager.getUser().getNotifyOnPersonalProgram() == 1) {
                shouldRegister = true;
            } else if (b && userManager.getUser().getNotifyOnPersonalProgram() == 0) {
                shouldRegister = true;
            }

            if (shouldRegister) {
                userManager.getUser().setNotifyOnPersonalProgram(b ? 1 : 0);
                userManager.registerUser();
            }
            decideProgramListVisibility(recyclerView);
        });

        // Set the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerViewContext));

        Map<String, ProgramInfo> personalProgramInfoMap = new HashMap<>(RaaContext.getInstance().getProgramInfoDirectory().getProgramInfoMap());
        Set<String> programNames = new HashSet<>(personalProgramInfoMap.keySet());
        for (String pName : programNames) {
            ProgramInfo ppi = personalProgramInfoMap.get(pName);
            if (ppi.getFeed() == null || !ppi.getFeed().equals("Personal")) {
                //noinspection SuspiciousMethodCalls
                personalProgramInfoMap.remove(pName);
            }
        }
        recyclerView.setAdapter(new
                PersonalProgramNotificationSettingsListRecyclerViewAdapter(
                personalProgramInfoMap,
                (programId, newValue) -> {
                    if (userManager.getUser().getNotifyOnPublicProgram() == 1) {
                        userManager.getUser().getNotificationExcludedPersonalPrograms().put(programId, !newValue);
                        userManager.registerUser();
                    }
                })
        );
        decideProgramListVisibility(recyclerView);

        return view;
    }

    private void decideProgramListVisibility(RecyclerView listView) {
        if (userManager.getUser().getNotifyOnPersonalProgram() == 1) {
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_personal_program_settings);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnPersonalProgramSettingsChangeListener {
        void onNotificationSettingsChanged(String programId, boolean newValue);
    }
}
