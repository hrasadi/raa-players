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
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.feed.PersonalFeedEntry;
import media.raa.raa_android_player.view.ProgramCardListItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link PersonalFeedEntry}
 */
public class PersonalFeedListSection extends StatelessSection {
    private final List<PersonalFeedEntry> personalFeed;
    private final SectionedRecyclerViewAdapter sectionedAdapter;

    private int expandedItemPosition = -1;

    PersonalFeedListSection(Feed feed, SectionedRecyclerViewAdapter sectionedAdapter) {
        super(new SectionParameters.Builder(R.layout.fragment_program_card)
                .headerResourceId(R.layout.feed_section_header)
                .footerResourceId(R.layout.feed_section_footer)
                .build());
        personalFeed = feed.getPersonalFeed();
        this.sectionedAdapter = sectionedAdapter;
    }

    @Override
    public PersonalFeedCardListItem getItemViewHolder(View view) {
        return new PersonalFeedCardListItem(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        PersonalFeedCardListItem personalFeedItem = (PersonalFeedCardListItem) holder;
        personalFeedItem.setPersonalFeedEntry(personalFeed.get(position));

        // HANDLE EXPANSION
        final boolean isExpanded = (position == this.expandedItemPosition);
        personalFeedItem.detailsView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        personalFeedItem.itemView.setOnClickListener(v -> {
            this.expandedItemPosition = isExpanded ? -1 : position;
            sectionedAdapter.notifyItemChangedInSection("PERSONAL_FEED", position);
        });
    }

    @Override
    public int getContentItemsTotal() {
        if (personalFeed != null) {
            return personalFeed.size();
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
        ((FeedListViewFragment.FeedSectionHeader) holder).setSectionHeaderLabel(R.string.personal_feed_section_header);
    }

    // Section Footer
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
            ((FeedListViewFragment.FeedSectionFooter) holder).setSectionFooterLabel(R.string.personal_feed_section_footer);
        }
    }

    public static class PersonalFeedCardListItem extends ProgramCardListItem {
        private PersonalFeedEntry personalFeedEntry;

        PersonalFeedCardListItem(View view) {
            super(view);

            this.startTimeTitleLbl.setText("شروع");
            this.endTimeTitleLbl.setText("پایان");
        }

        void setPersonalFeedEntry(PersonalFeedEntry personalFeedEntry) {
            this.personalFeedEntry = personalFeedEntry;

            if (this.personalFeedEntry != null) {
                // Set background if exists
                ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                        .getProgramInfoMap().get(this.personalFeedEntry.getProgram().getProgramId());
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

                this.programStartTimeView.setText(this.personalFeedEntry.getFormattedReleaseTime());
                this.programStartDayView.setText(this.personalFeedEntry.getReleaseTimeRelativeDay());

                this.programEndTimeView.setText(this.personalFeedEntry.getFormattedExpirationTime());
                this.programEndDayView.setText(this.personalFeedEntry.getExpirationTimeRelativeDay());

                this.programTitleView.setText(this.personalFeedEntry.getProgram().getTitle());
                this.programSubtitleView.setText(this.personalFeedEntry.getProgram().getSubtitle());

                if (personalFeedEntry.isInProgress()) {
                    this.actionButton.setVisibility(View.VISIBLE);
                } else {
                    this.actionButton.setVisibility(View.GONE);
                }
                this.actionButton.setOnClickListener(sender -> {
                    // Play public feed
                    Log.i("Raa", "Playback requested for personal entry: " + this.personalFeedEntry.getId());
                });
            }
        }
    }
}