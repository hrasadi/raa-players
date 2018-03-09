package media.raa.raa_android_player.model.notifications;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import media.raa.raa_android_player.model.RaaContext;
import media.raa.raa_android_player.model.user.UserManager;

/**
 * Messaging manager
 * Created by hamid on 10/6/17.
 */

public class MessagingInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("Raa", "Token is: " + refreshedToken);

        // Init context if it is has not already
        RaaContext.initializeInstance(getApplicationContext());

        // Conditional to handle concurrency issues
        if (RaaContext.getInstance().getUserManager().getUser() != null) {
            RaaContext.getInstance().getUserManager().getUser().setNotificationToken(refreshedToken);
            RaaContext.getInstance().getUserManager()
                    .notifyLoadingTaskDone(UserManager.LOADING_STEP_REFRESH_NOTIFICATION_TOKEN);
        }
    }
}
