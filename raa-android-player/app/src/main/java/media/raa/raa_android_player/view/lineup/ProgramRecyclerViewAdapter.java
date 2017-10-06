package media.raa.raa_android_player.view.lineup;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.lineup.Lineup;
import media.raa.raa_android_player.model.lineup.Program;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program}
 */
@SuppressWarnings("WeakerAccess")
public class ProgramRecyclerViewAdapter extends RecyclerView.Adapter<ProgramRecyclerViewAdapter.ViewHolder> {

    private final List<Program> mValues;

    public ProgramRecyclerViewAdapter(Lineup lineup) {
        mValues = lineup.getCurrentLineup();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_program, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mProgramTimeView.setText(mValues.get(position).programTime);
        holder.mProgramNameView.setText(mValues.get(position).programName);
        holder.mProgramClipsView.setText(mValues.get(position).programClips);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mProgramTimeView;
        public final TextView mProgramNameView;
        public final TextView mProgramClipsView;
        public Program mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mProgramTimeView = view.findViewById(R.id.programTime);
            mProgramNameView = view.findViewById(R.id.programName);
            mProgramClipsView = view.findViewById(R.id.programClips);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mProgramTimeView.getText() + "'";
        }
    }
}
