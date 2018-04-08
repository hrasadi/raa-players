package media.raa.raa_android_player.view.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;

/**
 * RecyclerView for archive directory list
 */
public class PersonalProgramNotificationSettingsListRecyclerViewAdapter extends RecyclerView.Adapter<PersonalProgramNotificationSettingsListRecyclerViewAdapter.PersonalProgramNotificationSettingsListItem> {

    private final Map<String, ProgramInfo> personalProgramInfoMap;
    private List<String> personalProgramInfoIds;

    private PersonalProgramNotificationSettingsListFragment.OnPersonalProgramSettingsChangeListener personalProgramSettingsChangeListener;

    PersonalProgramNotificationSettingsListRecyclerViewAdapter(Map<String, ProgramInfo> personalProgramInfos, PersonalProgramNotificationSettingsListFragment.OnPersonalProgramSettingsChangeListener personalProgramSettingsChangeListener) {
        this.personalProgramInfoMap = personalProgramInfos;
        this.personalProgramInfoIds = new ArrayList<>(personalProgramInfos.keySet());

        this.personalProgramSettingsChangeListener = personalProgramSettingsChangeListener;
    }

    @Override
    public PersonalProgramNotificationSettingsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_notification_settings_list_item, parent, false);
        return new PersonalProgramNotificationSettingsListItem(view);
    }

    @Override
    public void onBindViewHolder(PersonalProgramNotificationSettingsListItem holder, int position) {
        String programId = personalProgramInfoIds.get(position);
        holder.setProgramId(programId, personalProgramInfoMap.get(programId));
        holder.getProgramNotificationSettingsSwitch().setOnCheckedChangeListener((compoundButton, b) -> {
            if (personalProgramSettingsChangeListener != null) {
                personalProgramSettingsChangeListener
                        .onNotificationSettingsChanged(holder.getProgramId(), b);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (personalProgramInfoMap != null) {
            return personalProgramInfoMap.size();
        } else {
            return 0;
        }
    }

    public static class PersonalProgramNotificationSettingsListItem extends RecyclerView.ViewHolder {
        private String programId;

        private TextView programTitleLbl;
        private ImageView programThumbnailImageView;

        private Switch programNotificationSettingsSwitch;

        PersonalProgramNotificationSettingsListItem(View view) {
            super(view);

            this.programTitleLbl = view.findViewById(R.id.program_name);
            this.programThumbnailImageView = view.findViewById(R.id.program_thumbnail);
            this.programNotificationSettingsSwitch = view.findViewById(R.id.notify_on_programs_switch);
        }

        void setProgramId(String programId, ProgramInfo pInfo) {
            this.programId = programId;

            // Bind item
            if (pInfo != null) {
                programTitleLbl.setText(pInfo.getTitle());
                if (pInfo.getThumbnailBitmap() != null) {
                    programThumbnailImageView.setImageBitmap(pInfo.getThumbnailBitmap());
                } else {
                    programThumbnailImageView.setImageResource(R.drawable.img_default_thumbnail);
                }

                boolean isNotificationOn =
                        !(RaaContext.getInstance().getUserManager().getUser()
                                .getNotificationExcludedPersonalPrograms().containsKey(programId)) ||
                                !(RaaContext.getInstance().getUserManager().getUser()
                                        .getNotificationExcludedPersonalPrograms().get(programId));

                programNotificationSettingsSwitch.setChecked(isNotificationOn);
            } else {
                programTitleLbl.setText("");
                programThumbnailImageView.setImageResource(R.drawable.img_default_thumbnail);
            }
        }

        Switch getProgramNotificationSettingsSwitch() {
            return programNotificationSettingsSwitch;
        }

        String getProgramId() {
            return programId;
        }

        @Override
        public String toString() {
            return this.programId;
        }
    }
}
