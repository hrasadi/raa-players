package media.raa.raa_android_player.model.playback;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.view.player.NotificationBarPlayerControls;

import static media.raa.raa_android_player.model.playback.PlaybackManager.ACTION_PLAYBACK_FINISHED;

/**
 * Playback manager class implemented in the form of an Android IntentService.
  */
public class PlaybackService extends Service {
    private static final int RAA_SERVICE_FOREGROUND_ID = 1;

    public static final String ACTION_PLAY = "media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PLAY";
    public static final String ACTION_PAUSE = "media.raa.raa_android_player.model.playback.PlaybackService.ACTION_PAUSE";
    public static final String ACTION_RESUME = "media.raa.raa_android_player.model.playback.PlaybackService.ACTION_RESUME";
    public static final String ACTION_STOP = "media.raa.raa_android_player.model.playback.PlaybackService.ACTION_STOP";

    public static final String APP_ENTERING_BACKGROUND = "app_entering_background";

    private DefaultDataSourceFactory dataSourceFactory;
    private SimpleExoPlayer exoPlayer;

    private NotificationBarPlayerControls notificationBarPlayerControls;

    // Used for reading playback service data from main app
    private static PlaybackService instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        // If the application is not running (and no context present) this is our chance to recreate
        // the RaaContext
        RaaContext.initializeInstance(getApplicationContext());

        this.notificationBarPlayerControls = new NotificationBarPlayerControls(getApplicationContext());

        // ExoPlayer setup
        TrackSelection.Factory audioTrackSelectionFactory = new FixedTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(audioTrackSelectionFactory);

        String userAgent = Util.getUserAgent(getApplicationContext(), getApplicationInfo().name);
        this.dataSourceFactory = createDataSourceFactory(getApplicationContext(), userAgent);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(getApplicationContext(), trackSelector);
        exoPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                super.onPlayerStateChanged(playWhenReady, playbackState);

                if (playbackState == Player.STATE_ENDED) {
                    // Notify manager that playback ended
                    Intent playbackEndedIntent = new Intent(ACTION_PLAYBACK_FINISHED);
                    sendBroadcast(playbackEndedIntent);
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Objects.equals(intent.getAction(), APP_ENTERING_BACKGROUND)) {
            stopPlaybackIfNotificationsNotEnabled();
        } else {
            handlePlaybackIntent(intent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void stopPlaybackIfNotificationsNotEnabled() {
        if (notificationBarPlayerControls.areNotificationEnabled()) {
            startForeground(RAA_SERVICE_FOREGROUND_ID, this.notificationBarPlayerControls.getCurrentNotification());
        } else {
            // Stop the playback. There is no point in playing in background when we cannot show
            // media controls
            exoPlayer.stop(true);
        }
    }

    protected void handlePlaybackIntent(@Nullable final Intent intent) {
        if (intent == null) {
            return;
        }

        PlaybackManager.PlayerStatus currentPlayerStatus = RaaContext.getInstance()
                .getPlaybackManager().getCurrentPlayerStatus();

        AsyncTask.execute(() -> {
            if (Objects.equals(intent.getAction(), ACTION_PLAY)) {
                if (currentPlayerStatus != null) {
                    ExtractorMediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Uri.parse(currentPlayerStatus.getMediaSourceUrl()));
                    exoPlayer.prepare(mediaSource);
                    // Seek to position if needed
                    if (intent.hasExtra("seekTo")) {
                        exoPlayer.seekTo(intent.getLongExtra("seekTo", 0L));
                    }
                    exoPlayer.setPlayWhenReady(true);

                }
            } else if (Objects.equals(intent.getAction(), ACTION_PAUSE)) {
                exoPlayer.setPlayWhenReady(false);
            } else if (Objects.equals(intent.getAction(), ACTION_RESUME)) {
                exoPlayer.setPlayWhenReady(true);
            } else if (Objects.equals(intent.getAction(), ACTION_STOP)) {
                exoPlayer.stop(true);
            }

            notificationBarPlayerControls.updateNotificationBarPlayerControls(currentPlayerStatus);
        });
    }

    public static long getCurrentPlaybackPosition() {
        if (instance != null && instance.exoPlayer != null) {
            return instance.exoPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * Returns a DataSourceFactory that follows redirection.
     */
    public static DefaultDataSourceFactory createDataSourceFactory(Context context,
                                                                   String userAgent) {
        // Default parameters, except allowCrossProtocolRedirects is true
        DefaultHttpDataSourceFactory httpDataSourceFactory = new DefaultHttpDataSourceFactory(
                userAgent,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
        );

        return new DefaultDataSourceFactory(
                context,
                null,
                httpDataSourceFactory
        );
    }
}
