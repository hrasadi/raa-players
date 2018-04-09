package media.raa.raa_android_player.view.archive;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.entities.archive.ArchiveEntry;
import media.raa.raa_android_player.model.playback.PlaybackManager;
import media.raa.raa_android_player.view.ProgramCardListItemUtils;

public class ArchiveProgramListViewFragment extends Fragment implements PlaybackManager.PlaybackManagerEventListener {

    private String programId;
    List<ArchiveEntry> programArchive;

    public static ArchiveProgramListViewFragment newInstance(String programId) {
        ArchiveProgramListViewFragment archiveProgramListViewFragment = new ArchiveProgramListViewFragment();

        Bundle archiveProgramArgs = new Bundle();
        archiveProgramArgs.putString("programId", programId);
        archiveProgramListViewFragment.setArguments(archiveProgramArgs);

        return archiveProgramListViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.programId = getArguments().getString("programId", null);

        this.registerPlaybackManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archive_program_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;

            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            if (programId != null) {
                programArchive = RaaContext.getInstance().getArchive().getProgramArchive(programId);
                ArchiveProgramListRecyclerViewAdapter archiveProgramRecyclerAdapter =
                        new ArchiveProgramListRecyclerViewAdapter(programArchive);
                recyclerView.setAdapter(archiveProgramRecyclerAdapter);
            }
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
        RecyclerView recyclerView = (RecyclerView) this.getView();

        if (programArchive != null && recyclerView != null) {
            for (int i = 0; i < programArchive.size(); i++) {
                try {
                    ArchiveProgramListRecyclerViewAdapter.ArchiveProgramCardListItem itemViewHolder =
                            (ArchiveProgramListRecyclerViewAdapter.ArchiveProgramCardListItem)
                            recyclerView.findViewHolderForAdapterPosition(i);

                    if (!newStatus.isPlaying()) {
                        itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.PLAYABLE);
                    } else {
                        if (itemViewHolder.getArchiveEntry().getMainMediaSourceUrl().equals(newStatus.getMediaSourceUrl())) {
                            itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.CURRENTLY_PLAYING);
                        } else {
                            itemViewHolder.setActionButtonMode(ProgramCardListItemUtils.PlayableState.PLAYABLE);
                        }
                    }
                } catch (NullPointerException e) {
                    // No view for this index, just move on
                }
            }
        }
    }
}
