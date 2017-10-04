package media.raa.raa_android_player.model;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PlaybackService extends IntentService {
    private static final String STREAM_URL = "https://stream.raa.media/raa1.ogg";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_START = "media.raa.raa_android_player.model.action.START";
    private static final String ACTION_FOO = "media.raa.raa_android_player.model.action.FOO";
    private static final String ACTION_BAZ = "media.raa.raa_android_player.model.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "media.raa.raa_android_player.model.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "media.raa.raa_android_player.model.extra.PARAM2";

    private MediaPlayer player;

    public PlaybackService() {
        super("PlaybackService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        player = new MediaPlayer();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (intent.getAction().equals(ACTION_START)) {
            play();
        }

        return START_STICKY;
    }

    public void play() {
        if (player == null) {
            player = new MediaPlayer();
            try {
                player.setDataSource(STREAM_URL);
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        player.start();
                    }
                });
                player.prepareAsync();
            } catch (IOException e) {
                Log.e("Raa", "Error while initializing player", e);
            }
        } else {
            player.start();
        }
    }

    private void stop() {
        player.pause();
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
