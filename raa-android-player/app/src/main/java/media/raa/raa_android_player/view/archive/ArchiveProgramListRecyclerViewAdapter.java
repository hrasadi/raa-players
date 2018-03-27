package media.raa.raa_android_player.view.archive;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.archive.ArchiveEntry;
import media.raa.raa_android_player.view.ProgramCardListItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class ArchiveProgramListRecyclerViewAdapter extends
        RecyclerView.Adapter<ArchiveProgramListRecyclerViewAdapter.ArchiveProgramCardListItem> {
    private final List<ArchiveEntry> programArchive;

    private int expandedItemPosition = -1;

    ArchiveProgramListRecyclerViewAdapter(List<ArchiveEntry> programArchive) {
        this.programArchive = programArchive;
    }

    @Override
    public ArchiveProgramCardListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_card, parent, false);
        return new ArchiveProgramCardListItem(view);
    }

    @Override
    public void onBindViewHolder(final ArchiveProgramCardListItem holder, int position) {
        holder.setArchiveEntry(programArchive.get(position));

        // HANDLE EXPANSION
        final boolean isExpanded = (position == this.expandedItemPosition);
        holder.detailsView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            this.expandedItemPosition = isExpanded ? -1 : position;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        if (programArchive != null) {
            return programArchive.size();
        } else {
            return 0;
        }
    }

    static class ArchiveProgramCardListItem extends ProgramCardListItem {
        private ArchiveEntry archiveEntry;

        ArchiveProgramCardListItem(View view) {
            super(view);

            this.startTimeTitleLbl.setText("انتشار");
            this.endTimeTitleLbl.setText("");
        }

        void setArchiveEntry(ArchiveEntry archiveEntry) {
            this.archiveEntry = archiveEntry;

            if (this.archiveEntry != null) {
                // Set background if exists
                ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                        .getProgramInfoMap().get(this.archiveEntry.getProgram().getProgramId());
                if (pInfo != null && pInfo.getAbout() != null && !pInfo.getAbout().isEmpty()) {
                    programDetailsLbl.setText(pInfo.getAbout());
                } else {
                    programDetailsLbl.setText(R.string.default_program_description);
                }
                if (pInfo != null && pInfo.getBannerBitmap() != null) {
                    this.programBanner.setImageBitmap(pInfo.getBannerBitmap());
                } else {
                    this.programBanner.setImageResource(R.drawable.img_default_banner);
                }

                this.programStartTimeView.setText(this.archiveEntry.getFormattedReleaseDate());

                this.programStartDayView.setText("");
                this.programEndTimeView.setText("");
                this.programEndDayView.setText("");

                this.programTitleView.setText(this.archiveEntry.getProgram().getTitle());
                this.programSubtitleView.setText(this.archiveEntry.getProgram().getSubtitle());

                this.actionButton.setVisibility(View.VISIBLE);
                this.actionButton.setOnClickListener(sender -> {
                    // Play public feed
                    Log.i("Raa", "Playback requested for archive entry: " + this.archiveEntry.getProgram().getCanonicalIdPath());
                    RaaContext.getInstance().getPlaybackManager().playArchiveEntry(archiveEntry);
                });
            }
        }
    }
}