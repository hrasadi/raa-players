package media.raa.raa_android_player.view.player;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import media.raa.raa_android_player.R;
import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.playback.PlaybackManager;

public class InAppPlayerControlsView extends FrameLayout implements PlaybackManager.PlaybackManagerEventListener {

    private TextView programTitle;
    private TextView programSubTitle;
    private ImageView programThumbnail;

    private ImageButton playerActionBtn;

    public InAppPlayerControlsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
        registerPlaybackManager();
    }

    private void initView() {
        inflate(getContext(), R.layout.in_app_player_bar_controls, this);

        programTitle = findViewById(R.id.player_bar_program_title);
        programSubTitle = findViewById(R.id.player_bar_program_subtitle);
        programThumbnail = findViewById(R.id.player_bar_program_thumbnail);

        ImageButton playerCancelBtn = findViewById(R.id.player_bar_cancel_button);
        playerActionBtn = findViewById(R.id.player_bar_action_button);

        // Cancel playback
        playerCancelBtn.setOnClickListener(sender ->
                RaaContext.getInstance().getPlaybackManager().stop());
        // Play/pause Button listeners
        playerActionBtn.setOnClickListener(sender ->
                RaaContext.getInstance().getPlaybackManager().togglePlaybackState());
    }

    private void registerPlaybackManager() {
        RaaContext.getInstance().getPlaybackManager().registerPlaybackManagerEventListener(this);
    }

    @Override
    public void onPlayerStatusChange(PlaybackManager.PlayerStatus newStatus) {
        // OK. something happened to the playback. Let's display it!
        if (newStatus.isEnabled()) {
            // Display the bar
            this.setVisibility(VISIBLE);

            programTitle.setText(newStatus.getItemTitle());
            programSubTitle.setText(newStatus.getItemSubtitle());

            if (newStatus.getItemThumbnail() != null) {
                programThumbnail.setImageBitmap(newStatus.getItemThumbnail());
            } else {
                programThumbnail.setImageResource(R.drawable.img_default_thumbnail);
            }

            playerActionBtn.setImageResource(newStatus.isPlaying()
                    ? R.drawable.ic_pause_black_24dp : R.drawable.ic_play_black_24dp);
        } else {
            this.setVisibility(GONE);
        }
    }
}
