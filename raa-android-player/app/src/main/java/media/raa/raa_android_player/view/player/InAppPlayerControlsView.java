package media.raa.raa_android_player.view.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

        playerActionBtn = findViewById(R.id.player_bar_action_button);

        // Play/pause Button listeners
        playerActionBtn.setOnClickListener(sender -> {
            RaaContext.getInstance().getPlaybackManager().togglePlaybackState();
        });
    }

    private void registerPlaybackManager() {
        RaaContext.getInstance().getPlaybackManager().setPlaybackManagerEventListener(this);
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

//    public void startPlayerBar() {
//        // Also register the listener for the playback status bar
//        LocalBroadcastManager.getInstance(this).registerReceiver(metadataUpdateEventReceiver,
//                new IntentFilter(PLAYER_BAR_EVENT)
//        );
//    }

//    private void stopPlayerBar() {
//        // Stop any timers
//        playerBarTimer.purge();
//        playerBarTimer.cancel();
//
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(metadataUpdateEventReceiver);
//    }

    class MetadataUpdateEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // OK. Something has changed. Let's figure our what to do!
            // first let's update the bar title
//            final LiveBroadcastStatus playbackStatus = RaaContext.getInstance().getCurrentStatus(false);
//            if (!playbackStatus.isCurrentlyPlaying()) {
//                if (playbackStatus.getNextBoxId() == null) {
//                    // No more programs for today
//                    ((TextView) findViewById(R.id.player_bar_program_title))
//                            .setText(R.string.status_program_finish);
//                } else {
//                    long counterInMs = playbackStatus.getNextBoxStartTime().getTime() - new Date().getTime();
//                    final long counterInSec = TimeUnit.MILLISECONDS.toSeconds(counterInMs);
//
//                    // Show the countdown (update every one second)
//                    playerBarTimer.schedule(new TimerTask() {
//
//                        private long counter = counterInSec;
//
//                        @Override
//                        public void run() {
//                            String timeRemainingString = "";
//
//                            if (counter > 0) {
//                                counter--;
//
//                                if (counter / 3600 != 0) {
//                                    timeRemainingString = timeRemainingString + counter / 3600 + " ساعت و ";
//                                }
//                                long remaining = counter % 3600;
//                                if (remaining / 60 != 0) {
//                                    timeRemainingString = timeRemainingString + remaining / 60 + " دقیقه و ";
//                                }
//                                remaining = remaining % 60;
//                                timeRemainingString = timeRemainingString + remaining + " ثانیه ";
//
//                                timeRemainingString = Utils.convertToPersianLocaleString(timeRemainingString);
//                                timeRemainingString = String.format("%s در %s", playbackStatus.getNextBoxId(), timeRemainingString);
//
//                            } else {
//                                timeRemainingString = String.format("به زودی: %s", playbackStatus.getNextBoxId());
//                                this.cancel();
//                            }
//
//                            final String counterString = timeRemainingString;
//                            RaaMainActivity.this.runOnUiThread(() -> ((TextView) findViewById(R.id.player_bar_program_title))
//                                    .setText(counterString));
//                        }
//                    }, 0, 1000);
//                }

//            } else {
//                // cancel any previous timers
//                playerBarTimer.cancel();
//
//                // Now playing + current program name
//                ((TextView) findViewById(R.id.player_bar_program_title))
//                        .setText(String.format(getResources().getString(R.string.status_now_playing),
//                                playbackStatus.getCurrentProgram()));
//            }
        }
    }
}
