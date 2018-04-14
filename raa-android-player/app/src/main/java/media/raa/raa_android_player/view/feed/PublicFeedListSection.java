package media.raa.raa_android_player.view.feed;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.RaaMainActivity;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;
import media.raa.raa_android_player.model.playback.PlaybackManager;
import media.raa.raa_android_player.view.PlaybackModeRequesterPopup;
import media.raa.raa_android_player.view.ProgramCardListItem;
import media.raa.raa_android_player.view.ProgramCardListItemUtils;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class PublicFeedListSection extends StatelessSection {
    private final List<PublicFeedEntry> publicFeed;
    private final SectionedRecyclerViewAdapter sectionedAdapter;

    private FeedListViewFragment parentFragment;

    private int expandedItemPosition = -1;

    PublicFeedListSection(Feed feed, SectionedRecyclerViewAdapter sectionedAdapter, FeedListViewFragment parentFragment) {
        super(new SectionParameters.Builder(R.layout.fragment_program_card)
                .headerResourceId(R.layout.feed_section_header)
                .footerResourceId(R.layout.feed_section_footer)
                .build());
        publicFeed = feed.getPublicFeed();
        this.sectionedAdapter = sectionedAdapter;
        this.parentFragment = parentFragment;
    }

    @Override
    public PublicFeedCardListItem getItemViewHolder(View view) {
        return new PublicFeedCardListItem(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        PublicFeedCardListItem publicFeedItem = (PublicFeedCardListItem) holder;
        publicFeedItem.setPublicFeedEntry(publicFeed.get(position));
        publicFeedItem.setSection(this);

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

    public void updateItemsStatus(PlaybackManager.PlayerStatus newStatus, RecyclerView recyclerView) {
        if (publicFeed != null && recyclerView != null) {
            for (int i = 0; i < publicFeed.size(); i++) {
                try {
                    int adaptorPosition = sectionedAdapter.getPositionInAdapter("PUBLIC_FEED", i);
                    PublicFeedCardListItem itemViewHolder =
                            (PublicFeedCardListItem) recyclerView.findViewHolderForAdapterPosition(adaptorPosition);

                    if (!newStatus.isPlaying()) {
                        itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.PLAYABLE);
                    } else {
                        if (itemViewHolder.getPublicFeedEntry().getMainMediaSourceUrl().equals(newStatus.getMediaSourceUrl())) {
                            itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.CURRENTLY_PLAYING);
                        } else {
                            itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.PLAYABLE);
                        }
                    }
                } catch (NullPointerException | ClassCastException e) {
                    // No view for this index, just move on
                }
            }
        }
    }

    protected void onActionButtonClicked(PublicFeedListSection.PublicFeedCardListItem publicFeedCardListItem) {
        switch (ProgramCardListItemUtils.determineCardPlayableState(publicFeedCardListItem.getPublicFeedEntry().getMainMediaSourceUrl())) {
            case CURRENTLY_PLAYING:
                RaaContext.getInstance().getPlaybackManager().togglePlaybackState();
                break;
            case PLAYABLE:
                if (RaaContext.getInstance().getPlaybackManager()
                        .getLastPlaybackState(publicFeedCardListItem.getPublicFeedEntry().getMainMediaSourceUrl()) > 0) {

                    PlaybackModeRequesterPopup popup = PlaybackModeRequesterPopup
                            .newInstance(publicFeedCardListItem.getPublicFeedEntry().getRemainingDuration());

                    ((RaaMainActivity) parentFragment.getActivity()).setCurrentPlaybackModeRequesterCallback(
                            new PlaybackModeRequesterPopup.PlaybackModeRequesterCallback() {
                                @Override
                                public void onResumePlaybackRequested() {
                                    publicFeedCardListItem.getPublicFeedEntry().resumePlayback();
                                }

                                @Override
                                public void OnRestartPlaybackRequested() {
                                    publicFeedCardListItem.getPublicFeedEntry().restartPlayback();
                                }
                            });
                    popup.show(parentFragment.getFragmentManager(), "requester");
                } else {
                    publicFeedCardListItem.getPublicFeedEntry().restartPlayback();
                }
        }

    }

    public static class PublicFeedCardListItem extends ProgramCardListItem {
        private PublicFeedEntry publicFeedEntry;

        private PublicFeedListSection section;

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
                this.setActionButtonMode(ProgramCardListItemUtils
                        .determineCardPlayableState(this.publicFeedEntry.getMainMediaSourceUrl()));
                this.actionButton.setOnClickListener(sender ->
                        this.getSection().onActionButtonClicked(this));
            }
        }

        public PublicFeedEntry getPublicFeedEntry() {
            return publicFeedEntry;
        }

        PublicFeedListSection getSection() {
            return section;
        }

        void setSection(PublicFeedListSection section) {
            this.section = section;
        }
    }
}