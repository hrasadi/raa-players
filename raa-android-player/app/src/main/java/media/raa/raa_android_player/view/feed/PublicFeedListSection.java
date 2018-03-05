package media.raa.raa_android_player.view.feed;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.Feed;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;
import media.raa.raa_android_player.view.ProgramCardListItem;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
public class PublicFeedListSection extends StatelessSection {
    private final List<PublicFeedEntry> publicFeed;

    PublicFeedListSection(Feed feed) {
        super(new SectionParameters.Builder(R.layout.fragment_program_card)
                .headerResourceId(R.layout.feed_section_header)
                .footerResourceId(R.layout.feed_section_footer)
                .build());
        publicFeed = feed.getPublicFeed();
    }

    @Override
    public PublicFeedCardListItem getItemViewHolder(View view) {
        return new PublicFeedCardListItem(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, int position) {
        PublicFeedCardListItem publicFeedItem = (PublicFeedCardListItem) holder;
        publicFeedItem.setPublicFeedEntry(publicFeed.get(position));
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
            }
        }
    }
}