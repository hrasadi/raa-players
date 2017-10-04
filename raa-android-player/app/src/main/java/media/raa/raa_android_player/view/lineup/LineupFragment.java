package media.raa.raa_android_player.view.lineup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.lineup.Lineup;
import media.raa.raa_android_player.model.lineup.Program;

public class LineupFragment extends Fragment {

    private static final int ColumnCount = 1;
    private OnListFragmentInteractionListener mListener;

    private ProgramRecyclerViewAdapter programRecyclerAdapter;

    public LineupFragment() {
        // If the lineup is not received yet, register a callback
        RaaContext.getInstance().getLineup().setOnLineupLoadedCallback(new Lineup.LineupLoadedCallback() {
            @Override
            public void act() {
                programRecyclerAdapter.notifyDataSetChanged();
            }
        });
    }

    @SuppressWarnings("unused")
    public static LineupFragment newInstance() {
        return new LineupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_program_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            // I do not touch the generated code
            //noinspection ConstantConditions
            if (ColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, ColumnCount));
            }

            programRecyclerAdapter = new ProgramRecyclerViewAdapter(RaaContext.getInstance().getLineup(), mListener);
            recyclerView.setAdapter(programRecyclerAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(Program item);
    }

    /**
     * A fragment representing a list of Items.
     * <p/>
     * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
     * interface.
     */
    public static class LineupFragment extends Fragment {

        private static final int ColumnCount = 1;
        private OnListFragmentInteractionListener mListener;

        /**
         * Mandatory empty constructor for the fragment manager to instantiate the
         * fragment (e.g. upon screen orientation changes).
         */
        public LineupFragment() {
        }

        // TODO: Customize parameter initialization
        @SuppressWarnings("unused")
        public static LineupFragment newInstance() {
            LineupFragment fragment = new LineupFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_program_list, container, false);

            // Set the adapter
            if (view instanceof RecyclerView) {
                Context context = view.getContext();
                RecyclerView recyclerView = (RecyclerView) view;
                // I do not touch the generated code
                //noinspection ConstantConditions
                if (ColumnCount <= 1) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                } else {
                    recyclerView.setLayoutManager(new GridLayoutManager(context, ColumnCount));
                }

                recyclerView.setAdapter(new ProgramRecyclerViewAdapter.ProgramRecyclerViewAdapter(media.raa.raa_android_player.RaaContext.getInstance().getLineup(), mListener));
            }
            return view;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof OnListFragmentInteractionListener) {
                mListener = (OnListFragmentInteractionListener) context;
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement OnListFragmentInteractionListener");
            }
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;
        }

        public interface OnListFragmentInteractionListener {
            void onListFragmentInteraction(media.raa.raa_android_player.model.lineup.lineup.Program item);
        }
    }
}
