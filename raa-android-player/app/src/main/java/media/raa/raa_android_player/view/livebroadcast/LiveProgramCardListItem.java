package media.raa.raa_android_player.view.livebroadcast;

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

    LiveProgramCardListItem(View view) {
        super(view);

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
        }
    }
}
