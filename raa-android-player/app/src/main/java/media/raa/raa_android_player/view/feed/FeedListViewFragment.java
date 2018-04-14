package media.raa.raa_android_player.view.feed;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.playback.PlaybackManager;

public class FeedListViewFragment extends Fragment implements
        PlaybackManager.PlaybackManagerEventListener {
    public static FeedListViewFragment newInstance() {
        return new FeedListViewFragment();
    }

    private RecyclerView recyclerView;
    private PublicFeedListSection publicFeedListSection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.registerPlaybackManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            this.recyclerView = recyclerView;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            SectionedRecyclerViewAdapter sectionAdapter = new SectionedRecyclerViewAdapter();
            sectionAdapter.addSection("PERSONAL_FEED",
                    new PersonalFeedListSection(RaaContext.getInstance().getFeed(), sectionAdapter));
            this.publicFeedListSection =
                    new PublicFeedListSection(RaaContext.getInstance().getFeed(), sectionAdapter, this);
            sectionAdapter.addSection("PUBLIC_FEED", this.publicFeedListSection);

            recyclerView.setAdapter(sectionAdapter);
        }
        return view;
    }

    private void registerPlaybackManager() {
        RaaContext.getInstance().getPlaybackManager().registerPlaybackManagerEventListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPlayerStatusChange(PlaybackManager.PlayerStatus newStatus) {
        publicFeedListSection.updateItemsStatus(newStatus, recyclerView);
    }

    static class FeedSectionHeader extends RecyclerView.ViewHolder {
        private TextView sectionHeader;

        FeedSectionHeader(View view) {
            super(view);
            sectionHeader = view.findViewById(R.id.feed_section_header);
        }

        void setSectionHeaderLabel(int resId) {
            this.sectionHeader.setText(resId);
        }
    }

    static class FeedSectionFooter extends RecyclerView.ViewHolder {
        private TextView sectionFooter;

        FeedSectionFooter(View view) {
            super(view);
            sectionFooter = view.findViewById(R.id.feed_section_footer);
        }

        void setSectionFooterLabel(int resId) {
            this.sectionFooter.setText(resId);
        }
    }
}
