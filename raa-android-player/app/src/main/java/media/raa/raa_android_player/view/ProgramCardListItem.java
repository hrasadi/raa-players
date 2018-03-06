package media.raa.raa_android_player.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import media.raa.raa_android_player.R;

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

    protected final Button actionButton;

    public final View detailsView;
    protected final TextView programDetailsLbl;

    public ProgramCardListItem(View view) {
        super(view);
        this.view = view;

        programBanner = view.findViewById(R.id.program_card_banner);

        startTimeTitleLbl = view.findViewById(R.id.time_title1);
        endTimeTitleLbl = view.findViewById(R.id.time_title2);

        programStartTimeView = view.findViewById(R.id.time_value1);
        programStartDayView = view.findViewById(R.id.time_sub_value1);

        programEndTimeView = view.findViewById(R.id.time_value2);
        programEndDayView = view.findViewById(R.id.time_sub_value2);

        programTitleView = view.findViewById(R.id.program_title);
        programSubtitleView = view.findViewById(R.id.program_subtitle);

        actionButton = view.findViewById(R.id.program_action_full_btn);

        detailsView = view.findViewById(R.id.card_details_view);
        programDetailsLbl = view.findViewById(R.id.card_details_view_text);
    }

    @Override
    public String toString() {
        return super.toString() + " '" + programTitleView.getText() + "'";
    }

}
