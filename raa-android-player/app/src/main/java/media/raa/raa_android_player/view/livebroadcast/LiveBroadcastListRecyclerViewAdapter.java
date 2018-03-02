package media.raa.raa_android_player.view.livebroadcast;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.entities.Program;
import media.raa.raa_android_player.model.livebroadcast.LiveBroadcastLineup;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
@SuppressWarnings("WeakerAccess")
public class LiveBroadcastListRecyclerViewAdapter extends RecyclerView.Adapter<LiveBroadcastListRecyclerViewAdapter.ViewHolder> {

    private final List<Program> mValues;


    public LiveBroadcastListRecyclerViewAdapter(LiveBroadcastLineup lineup) {
        mValues = lineup.getFlatLineup();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mStartTimeTitleLbl.setText( "از");
        holder.mEndTimeTitleLbl.setText("تا");

        holder.mProgramStartTimeView.setText(mValues.get(position).getMetadata().getFormattedStartTime());
        holder.mProgramStartDayView.setText(mValues.get(position).getMetadata().getStartTimeRelativeDay());

        holder.mProgramEndTimeView.setText(mValues.get(position).getMetadata().getFormattedEndTime());
        holder.mProgramEndDayView.setText(mValues.get(position).getMetadata().getEndTimeRelativeDay());

        holder.mProgramNameView.setText(mValues.get(position).getTitle());
        holder.mProgramClipsView.setText(mValues.get(position).getSubtitle());
    }

    @Override
    public int getItemCount() {
        if (mValues != null) {
            return mValues.size();
        } else {
            return 0;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView mStartTimeTitleLbl;
        public final TextView mEndTimeTitleLbl;

        public final TextView mProgramStartTimeView;
        public final TextView mProgramStartDayView;

        public final TextView mProgramEndTimeView;
        public final TextView mProgramEndDayView;

        public final TextView mProgramNameView;
        public final TextView mProgramClipsView;
        public Program mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mStartTimeTitleLbl = view.findViewById(R.id.timeTitle1);
            mEndTimeTitleLbl = view.findViewById(R.id.timeTitle2);

            mProgramStartTimeView = view.findViewById(R.id.timeValue1);
            mProgramStartDayView = view.findViewById(R.id.timeSubValue1);

            mProgramEndTimeView = view.findViewById(R.id.timeValue2);
            mProgramEndDayView = view.findViewById(R.id.timeSubValue2);

            mProgramNameView = view.findViewById(R.id.programName);
            mProgramClipsView = view.findViewById(R.id.programClips);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mProgramStartTimeView.getText() + "'";
        }
    }
}
