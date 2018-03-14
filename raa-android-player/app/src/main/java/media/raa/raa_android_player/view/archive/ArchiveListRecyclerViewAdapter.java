package media.raa.raa_android_player.view.archive;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.archive.Archive;

/**
 * RecyclerView for archive directory list
 */
public class ArchiveListRecyclerViewAdapter extends RecyclerView.Adapter<ArchiveListRecyclerViewAdapter.ArchiveDirectoryListItem> {

    private final List<String> archiveDirectoryProgramIds;
    private ArchiveListFragment.OnArchiveListInteractionListener itemSelectionListener;

    ArchiveListRecyclerViewAdapter(Archive archiveDirectory, ArchiveListFragment.OnArchiveListInteractionListener itemSelectionListener) {
        this.archiveDirectoryProgramIds = archiveDirectory.getArchiveDirectoryProgramIds();
        this.itemSelectionListener = itemSelectionListener;
    }

    @Override
    public ArchiveDirectoryListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_archive_list_item, parent, false);
        return new ArchiveDirectoryListItem(view);
    }

    @Override
    public void onBindViewHolder(ArchiveDirectoryListItem holder, int position) {
        holder.setProgramId(archiveDirectoryProgramIds.get(position));
        holder.itemView.setOnClickListener(view -> {
            if (itemSelectionListener != null) {
                itemSelectionListener.onArchiveProgramSelected(holder.getProgramId());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (archiveDirectoryProgramIds != null) {
            return archiveDirectoryProgramIds.size();
        } else {
            return 0;
        }
    }

    public static class ArchiveDirectoryListItem  extends RecyclerView.ViewHolder {
        private String programId;

        private TextView programTitleLbl;
        private ImageView programThumbnailImageView;

        ArchiveDirectoryListItem(View view) {
            super(view);

            this.programTitleLbl = view.findViewById(R.id.program_name);
            this.programThumbnailImageView = view.findViewById(R.id.program_thumbnail);
        }

        void setProgramId(String programId) {
            this.programId = programId;

            // Bind item
            ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                    .getProgramInfoMap().get(programId);

            if (pInfo != null) {
                programTitleLbl.setText(pInfo.getTitle());
                if (pInfo.getThumbnailBitmap() != null) {
                    programThumbnailImageView.setImageBitmap(pInfo.getThumbnailBitmap());
                } else {
                    programThumbnailImageView.setImageResource(R.drawable.img_default_thumbnail);
                }
            } else {
                programTitleLbl.setText("");
                programThumbnailImageView.setImageResource(R.drawable.img_default_thumbnail);
            }
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
