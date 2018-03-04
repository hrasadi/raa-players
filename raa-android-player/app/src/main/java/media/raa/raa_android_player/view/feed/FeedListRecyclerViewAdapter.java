package media.raa.raa_android_player.view.feed;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class FeedListRecyclerViewAdapter extends RecyclerView.Adapter<PublicFeedCardListItem> {
    private final List<PublicFeedEntry> publicFeed;

    FeedListRecyclerViewAdapter(Feed feed) {
        publicFeed = feed.getPublicFeed();
    }

    @Override
    public PublicFeedCardListItem onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_card, parent, false);
        return new PublicFeedCardListItem(view);
    }

    @Override
    public void onBindViewHolder(final PublicFeedCardListItem holder, int position) {
        holder.setPublicFeedEntry(publicFeed.get(position));
    }

    @Override
    public int getItemCount() {
        if (publicFeed != null) {
            return publicFeed.size();
        } else {
            return 0;
        }
    }
}
