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
import java.util.Map;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.user.UserManager;

/**
 * Displays archive directory list
 */
public class PublicProgramNotificationSettingsListFragment extends Fragment {

    private UserManager userManager = RaaContext.getInstance().getUserManager();

    public static PublicProgramNotificationSettingsListFragment newInstance() {
        return new PublicProgramNotificationSettingsListFragment();
    }

    public PublicProgramNotificationSettingsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_public_program_notification_settings_list, container, false);

        Switch notifyOnPublicProgramsSwitch = view.findViewById(R.id.notify_on_public_programs_switch);
        RecyclerView recyclerView = view.findViewById(R.id.list);
        Context recyclerViewContext = recyclerView.getContext();

        // Set the parent switch
        notifyOnPublicProgramsSwitch.setChecked(userManager.getUser().getNotifyOnPublicProgram() == 1);
        notifyOnPublicProgramsSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            boolean shouldRegister = false;
            if (!b && userManager.getUser().getNotifyOnPublicProgram() == 1) {
                shouldRegister = true;
            } else if (b && userManager.getUser().getNotifyOnPublicProgram() == 0) {
                shouldRegister = true;
            }

            if (shouldRegister) {
                userManager.getUser().setNotifyOnPublicProgram(b ? 1 : 0);
                userManager.registerUser();
            }
            decideProgramListVisibility(recyclerView);
        });

        // Set the adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerViewContext));

        Map<String, ProgramInfo> publicProgramInfoMap = new HashMap<>(RaaContext.getInstance().getProgramInfoDirectory().getProgramInfoMap());
        for (ProgramInfo ppi : publicProgramInfoMap.values()) {
            if (ppi.getFeed() == null || !ppi.getFeed().equals("Public")) {
                //noinspection SuspiciousMethodCalls
                publicProgramInfoMap.remove(ppi);
            }
        }
        recyclerView.setAdapter(new
                PublicProgramNotificationSettingsListRecyclerViewAdapter(
                publicProgramInfoMap,
                (programId, newValue) -> {
                    if (userManager.getUser().getNotifyOnPublicProgram() == 1) {
                        userManager.getUser().getNotificationExcludedPublicPrograms().put(programId, !newValue);
                        userManager.registerUser();
                    }
                })
        );
        decideProgramListVisibility(recyclerView);

        return view;
    }

    private void decideProgramListVisibility(RecyclerView listView) {
        if (userManager.getUser().getNotifyOnPublicProgram() == 1) {
            listView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_public_program_settings);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnPublicProgramSettingsChangeListener {
        void onNotificationSettingsChanged(String programId, boolean newValue);
    }
}
