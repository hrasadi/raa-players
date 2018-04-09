package media.raa.raa_android_player.view.feed;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;
import media.raa.raa_android_player.view.ProgramCardListItem;
import media.raa.raa_android_player.view.ProgramCardListItemUtils;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class PublicFeedListSection extends StatelessSection {
    private final List<PublicFeedEntry> publicFeed;
    private final SectionedRecyclerViewAdapter sectionedAdapter;

    private int expandedItemPosition = -1;

    PublicFeedListSection(Feed feed, SectionedRecyclerViewAdapter sectionedAdapter) {
        super(new SectionParameters.Builder(R.layout.fragment_program_card)
                .headerResourceId(R.layout.feed_section_header)
                .footerResourceId(R.layout.feed_section_footer)
                .build());
        publicFeed = feed.getPublicFeed();
        this.sectionedAdapter = sectionedAdapter;
    }

    @Override
    public PublicFeedCardListItem getItemViewHolder(View view) {
        return new PublicFeedCardListItem(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        PublicFeedCardListItem publicFeedItem = (PublicFeedCardListItem) holder;
        publicFeedItem.setPublicFeedEntry(publicFeed.get(position));

        // HANDLE EXPANSION
        final boolean isExpanded = (position == this.expandedItemPosition);
        publicFeedItem.detailsView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        publicFeedItem.itemView.setOnClickListener(v -> {
            this.expandedItemPosition = isExpanded ? -1 : position;
            sectionedAdapter.notifyItemChangedInSection("PUBLIC_FEED", position);
        });
    }

    @Override
    public int getContentItemsTotal() {
        if (publicFeed != null) {
            return publicFeed.size();
        } else {
            return 0;
        }
    }

    // Section header
    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new FeedListViewFragment.FeedSectionHeader(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        ((FeedListViewFragment.FeedSectionHeader) holder).setSectionHeaderLabel(R.string.public_feed_section_header);
    }

    // Section footer
    @Override
    public RecyclerView.ViewHolder getFooterViewHolder(View view) {
        return new FeedListViewFragment.FeedSectionFooter(view);
    }

    @Override
    public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
        // Don't show footer if there are items to show
        if (getContentItemsTotal() != 0) {
            holder.itemView.setVisibility(View.GONE);
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            ((FeedListViewFragment.FeedSectionFooter) holder).setSectionFooterLabel(R.string.public_feed_section_footer);
        }
    }

    public static class PublicFeedCardListItem extends ProgramCardListItem {
        private PublicFeedEntry publicFeedEntry;

        PublicFeedCardListItem(View view) {
            super(view);

            this.startTimeTitleLbl.setText("انتشار");
            this.endTimeTitleLbl.setText("انقضاء");
        }

        void setPublicFeedEntry(PublicFeedEntry publicFeedEntry) {
            this.publicFeedEntry = publicFeedEntry;

            if (this.publicFeedEntry != null) {
                // Set background if exists
                ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                        .getProgramInfoMap().get(this.publicFeedEntry.getProgram().getProgramId());
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

                this.programStartTimeView.setText(this.publicFeedEntry.getFormattedReleaseTime());
                this.programStartDayView.setText(this.publicFeedEntry.getReleaseTimeRelativeDay());

                this.programEndTimeView.setText(this.publicFeedEntry.getFormattedExpirationTime());
                this.programEndDayView.setText(this.publicFeedEntry.getExpirationTimeRelativeDay());

                this.programTitleView.setText(this.publicFeedEntry.getProgram().getTitle());
                this.programSubtitleView.setText(this.publicFeedEntry.getProgram().getSubtitle());

                this.actionButton.setVisibility(View.VISIBLE);
                this.setActionButtonMode(ProgramCardListItemUtils.determineCardPlayableState(this.publicFeedEntry.getMainMediaSourceUrl()));
                this.actionButton.setOnClickListener(sender -> {
                    switch (ProgramCardListItemUtils.determineCardPlayableState(this.publicFeedEntry.getMainMediaSourceUrl())) {
                        case CURRENTLY_PLAYING:
                            RaaContext.getInstance().getPlaybackManager().togglePlaybackState();
                            break;
                        case PLAYABLE:
                            // Play public feed
                            Log.i("Raa", "Playback requested for public entry: " + this.publicFeedEntry.getId());
                            RaaContext.getInstance().getPlaybackManager().playPublicFeedEntry(publicFeedEntry);
                    }
                });
            }
        }
    }
}