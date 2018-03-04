package media.raa.raa_android_player.view.feed;

import android.view.View;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.model.entities.feed.PublicFeedEntry;
import media.raa.raa_android_player.view.ProgramCardListItem;

/**
 * Live program card
 * Created by hamid on 3/2/18.
 */

public class PublicFeedCardListItem extends ProgramCardListItem {
    private PublicFeedEntry publicFeedEntry;

    PublicFeedCardListItem(View view) {
        super(view);

        this.startTimeTitleLbl.setText( "انتشار");
        this.endTimeTitleLbl.setText("انقضاء");
    }

    public PublicFeedEntry getPublicFeedEntry() {
        return publicFeedEntry;
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
