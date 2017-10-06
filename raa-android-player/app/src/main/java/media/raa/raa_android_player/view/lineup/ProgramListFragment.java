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

public class ProgramListFragment extends Fragment {

    private static final int ColumnCount = 1;

    public ProgramListFragment() {
    }

    @SuppressWarnings("unused")
    public static ProgramListFragment newInstance() {
        return new ProgramListFragment();
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

            // lineup must be set now
            ProgramRecyclerViewAdapter programRecyclerAdapter = new ProgramRecyclerViewAdapter(RaaContext.getInstance().getCurrentLineup(false));
            recyclerView.setAdapter(programRecyclerAdapter);
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
