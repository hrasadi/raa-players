package media.raa.raa_android_player.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.entities.Program;

/**
 * Superclass for all types of program cards
 * Created by hamid on 3/2/18.
 */

public class ProgramCardListItem extends RecyclerView.ViewHolder {
    public final View view;

    protected ImageView programBanner;

    protected final TextView startTimeTitleLbl;
    protected final TextView endTimeTitleLbl;

    protected final TextView programStartTimeView;
    protected final TextView programStartDayView;

    protected final TextView programEndTimeView;
    protected final TextView programEndDayView;
    
    protected final TextView programTitleView;
    protected final TextView programSubtitleView;

    public final View detailsView;

    public ProgramCardListItem(View view) {
        super(view);
        this.view = view;

        programBanner = view.findViewById(R.id.program_card_banner);

        startTimeTitleLbl = view.findViewById(R.id.timeTitle1);
        endTimeTitleLbl = view.findViewById(R.id.timeTitle2);

        programStartTimeView = view.findViewById(R.id.timeValue1);
        programStartDayView = view.findViewById(R.id.timeSubValue1);

        programEndTimeView = view.findViewById(R.id.timeValue2);
        programEndDayView = view.findViewById(R.id.timeSubValue2);

        programTitleView = view.findViewById(R.id.programTitle);
        programSubtitleView = view.findViewById(R.id.programSubtitle);

        detailsView = view.findViewById(R.id.card_details_view);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + programTitleView.getText() + "'";
    }

}
