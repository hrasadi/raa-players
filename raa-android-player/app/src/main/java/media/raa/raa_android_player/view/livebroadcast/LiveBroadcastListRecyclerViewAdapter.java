package media.raa.raa_android_player.view.livebroadcast;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.livebroadcast.LiveBroadcastLineup;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class LiveBroadcastListRecyclerViewAdapter extends RecyclerView.Adapter<LiveProgramCardListItem> {
    private final List<Program> flatLineup;

    private int expandedItemPosition = -1;

    LiveBroadcastListRecyclerViewAdapter(LiveBroadcastLineup lineup) {
        flatLineup = lineup.getFlatLineup();
    }

    @Override
    public LiveProgramCardListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_card, parent, false);
        return new LiveProgramCardListItem(view);
    }

    @Override
    public void onBindViewHolder(final LiveProgramCardListItem holder, int position) {
        final boolean isExpanded = (position == this.expandedItemPosition);
        holder.detailsView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.setProgram(flatLineup.get(position));

        holder.itemView.setOnClickListener(v -> {
            this.expandedItemPosition = isExpanded ? -1 : position;
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        if (flatLineup != null) {
            return flatLineup.size();
        } else {
            return 0;
        }
    }
}
