package media.raa.raa_android_player.view.livebroadcast;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.entities.ProgramInfo;
import media.raa.raa_android_player.view.ProgramCardListItem;

/**
 * Live program card
 * Created by hamid on 3/2/18.
 */

public class LiveProgramCardListItem extends ProgramCardListItem {
    private Program program;
    private CardView cardView;

    LiveProgramCardListItem(View view) {
        super(view);

        cardView = view.findViewById(R.id.card_view);

        this.startTimeTitleLbl.setText("از");
        this.endTimeTitleLbl.setText("تا");
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;

        if (program != null) {
            // Set background if exists
            ProgramInfo pInfo = RaaContext.getInstance().getProgramInfoDirectory()
                    .getProgramInfoMap().get(this.program.getProgramId());
            if (pInfo != null && pInfo.getAbout() != null && !pInfo.getAbout().isEmpty()) {
                programDetailsLbl.setText(pInfo.getAbout());
            } else {
                programDetailsLbl.setText(R.string.default_program_description);
            }

            // Banner
            if (pInfo != null && pInfo.getBannerBitmap() != null) {
                this.programBanner.setImageBitmap(pInfo.getBannerBitmap());
            } else {
                this.programBanner.setImageResource(R.drawable.img_default_banner);
            }

            // Update values
            this.programStartTimeView.setText(this.program.getMetadata().getFormattedStartTime());
            this.programStartDayView.setText(this.program.getMetadata().getStartTimeRelativeDay());

            this.programEndTimeView.setText(this.program.getMetadata().getFormattedEndTime());
            this.programEndDayView.setText(this.program.getMetadata().getEndTimeRelativeDay());

            this.programTitleView.setText(this.program.getTitle());
            this.programSubtitleView.setText(this.program.getSubtitle());

            // Show overlay of passed
            if (isDisabled()) {
                this.cardView.setAlpha(0.3F);
                // Disable item
                this.cardView.setForeground(new ColorDrawable
                        (this.cardView.getResources().getColor(R.color.color_black_overlay)));
            } else {
                this.cardView.setAlpha(1F);
                this.cardView.setForeground(null);
            }

            // Show action button if next inline or in progress
            if (isInProgress()) {
                this.actionButton.setText(R.string.card_play);
                this.actionButton.setBackgroundColor(this.actionButton.getResources().getColor(R.color.color_primary));
                this.actionButton.setVisibility(View.VISIBLE);
                this.actionButton.setOnClickListener(sender -> {
                    // Play live feed
                    Log.i("Raa", "Playback requested for live stream.");
                    RaaContext.getInstance().getPlaybackManager().playLiveBroadcast();
                });

            } else if (isNextInLine()) {
                // TODO Counter
                this.actionButton.setText(R.string.live_card_soon);
                this.actionButton.setBackgroundColor(this.actionButton.getResources().getColor(R.color.color_live_card_next_in_line));
                this.actionButton.setVisibility(View.VISIBLE);
                // Make button un-clickable
                this.actionButton.setOnClickListener(null);
                }
            else {
                this.actionButton.setVisibility(View.GONE);
            }
        }
    }

    boolean isDisabled() {
        return program != null && program.getMetadata() != null &&
                program.getMetadata().hasFinished();
    }

    private boolean isInProgress() {
        return program != null && program.getMetadata() != null &&
                program.getMetadata().isInProgress();
    }

    private boolean isNextInLine() {
        return program != null && program.isNextInLine();
    }
}
