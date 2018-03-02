package media.raa.raa_android_player.view.livebroadcast;

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

public class LiveBroadcastListViewFragment extends Fragment {

    public static LiveBroadcastListViewFragment newInstance() {
        return new LiveBroadcastListViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_broadcast, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            LiveBroadcastListRecyclerViewAdapter programRecyclerAdapter = new LiveBroadcastListRecyclerViewAdapter(RaaContext.getInstance().getLiveBroadcastLineup());
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
