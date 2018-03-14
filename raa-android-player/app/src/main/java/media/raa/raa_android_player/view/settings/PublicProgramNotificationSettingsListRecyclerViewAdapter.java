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
public class PublicProgramNotificationSettingsListRecyclerViewAdapter extends RecyclerView.Adapter<PublicProgramNotificationSettingsListRecyclerViewAdapter.PublicProgramNotificationSettingsListItem> {

    private final Map<String, ProgramInfo> publicProgramInfoMap;
    private List<String> publicProgramInfoIds;

    private PublicProgramNotificationSettingsListFragment.OnPublicProgramSettingsChangeListener publicProgramSettingsChangeListener;

    PublicProgramNotificationSettingsListRecyclerViewAdapter(Map<String, ProgramInfo> publicProgramInfos, PublicProgramNotificationSettingsListFragment.OnPublicProgramSettingsChangeListener publicProgramSettingsChangeListener) {
        this.publicProgramInfoMap = publicProgramInfos;
        this.publicProgramInfoIds = new ArrayList<>(publicProgramInfos.keySet());

        this.publicProgramSettingsChangeListener = publicProgramSettingsChangeListener;
    }

    @Override
    public PublicProgramNotificationSettingsListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_notification_settings_list_item, parent, false);
        return new PublicProgramNotificationSettingsListItem(view);
    }

    @Override
    public void onBindViewHolder(PublicProgramNotificationSettingsListItem holder, int position) {
        String programId = publicProgramInfoIds.get(position);
        holder.setProgramId(programId, publicProgramInfoMap.get(programId));
        holder.getProgramNotificationSettingsSwitch().setOnCheckedChangeListener((compoundButton, b) -> {
            if (publicProgramSettingsChangeListener != null) {
                publicProgramSettingsChangeListener
                        .onNotificationSettingsChanged(holder.getProgramId(), b);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (publicProgramInfoMap != null) {
            return publicProgramInfoMap.size();
        } else {
            return 0;
        }
    }

    public static class PublicProgramNotificationSettingsListItem extends RecyclerView.ViewHolder {
        private String programId;

        private TextView programTitleLbl;
        private ImageView programThumbnailImageView;

        private Switch programNotificationSettingsSwitch;

        PublicProgramNotificationSettingsListItem(View view) {
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
                                .getNotificationExcludedPublicPrograms().containsKey(programId)) ||
                                !(RaaContext.getInstance().getUserManager().getUser()
                                        .getNotificationExcludedPublicPrograms().get(programId));

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
