package media.raa.raa_android_player.view.archive;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;

/**
 * Archive Program List Loading state
 * Created by hamid on 3/7/18.
 */
public class ArchiveProgramListLoadingFragment extends Fragment {

    private String programId;

    public static ArchiveProgramListLoadingFragment newInstance(String programId) {
        ArchiveProgramListLoadingFragment archiveProgramListLoadingFragment = new ArchiveProgramListLoadingFragment();

        Bundle archiveProgramArgs = new Bundle();
        archiveProgramArgs.putString("programId", programId);
        archiveProgramListLoadingFragment.setArguments(archiveProgramArgs);

        return archiveProgramListLoadingFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.programId = getArguments().getString("programId", null);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        if (programId != null) {
            //noinspection unchecked
            RaaContext.getInstance().getArchive().loadProgramArchive(this.programId).done(rs -> {
                //noinspection unchecked
                ArchiveProgramListViewFragment fragment =
                        ArchiveProgramListViewFragment.newInstance(programId);

                Handler mainHandler = new Handler(getContext().getMainLooper());
                mainHandler.post(() -> getFragmentManager().beginTransaction()
                        .addToBackStack(programId)
                        .replace(R.id.application_frame, fragment)
                        .commit());
            });
        }
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getTitle());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public String getTitle() {
        if (programId != null &&
                RaaContext.getInstance().getProgramInfoDirectory()
                        .getProgramInfoMap().get(programId) != null) {
            return RaaContext.getInstance().getProgramInfoDirectory()
                    .getProgramInfoMap().get(programId).getTitle();
        }
        return null;
    }
}
