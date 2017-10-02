package media.raa.raa_android_player;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import media.raa.raa_android_player.LineupFragment.OnListFragmentInteractionListener;
import media.raa.raa_android_player.lineup.Lineup;
import media.raa.raa_android_player.lineup.Program;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Program} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
@SuppressWarnings("WeakerAccess")
public class ProgramRecyclerViewAdapter extends RecyclerView.Adapter<ProgramRecyclerViewAdapter.ViewHolder> {

    private final List<Program> mValues;
    private final OnListFragmentInteractionListener mListener;

    public ProgramRecyclerViewAdapter(Lineup lineup, OnListFragmentInteractionListener listener) {
        mValues = lineup.getCurrentLineup();
        mListener = listener;
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

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
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
