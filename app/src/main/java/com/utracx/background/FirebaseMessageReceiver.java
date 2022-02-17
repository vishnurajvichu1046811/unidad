package com.utracx.background;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.utracx.R;
import com.utracx.api.model.rest.firebase.FirebaseRequestBody;
import com.utracx.api.request.ApiUtils;
import com.utracx.api.request.calls.FirebaseInstanceIdCall;
import com.utracx.database.AppDatabaseRepository;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.view.activity.AlertListActivity;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executors;

import static com.utracx.util.ConstantVariables.NOTIFICATION_REQUEST_CODE;
import static com.utracx.util.ConstantVariables.REMOTE_MESSAGE_BUNDLE_KEY;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 * <p>
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 * <p>
 * <intent-filter>
 * <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class FirebaseMessageReceiver extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0 || remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            if (remoteMessage.getNotification() != null) {
                Log.d(TAG, "Message notification title : " + remoteMessage.getNotification().getTitle());
                Log.d(TAG, "Message notification title : " + remoteMessage.getNotification().getBody());
            }
            showLocalNotification(remoteMessage);
        }
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(@NotNull String token) {

        // Save in DB
        Executors.newSingleThreadExecutor().execute(
                () ->
                        AppDatabaseRepository
                                .getInstance(getApplicationContext())
                                .updateFirebaseInstanceID(token)
        );

        //To manage subscriptions for device alerts/Push on the server side,
        // sending the Instance ID token to your app server.
        registerFirebaseInstanceID();
    }

    private void registerFirebaseInstanceID() {
        Executors.newSingleThreadExecutor().execute(
                () -> {

                    UserDataEntity userData = AppDatabaseRepository.getInstance(getApplicationContext()).getUserData();
                    if (userData != null) {
                        FirebaseRequestBody firebaseRequestBody = new FirebaseRequestBody(
                                userData.getUsername(),
                                userData.getPassword(),
                                userData.getFirebaseInstanceID()
                        );

                        ApiUtils
                                .getInstance()
                                .getSOService()
                                .registerFirebaseForUser(firebaseRequestBody)
                                .enqueue(new FirebaseInstanceIdCall(firebaseRequestBody));
                    }
                }
        );
    }
    // [END on_new_token]


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param remoteMessage FCM message received.
     */
    private void showLocalNotification(RemoteMessage remoteMessage) {

        String notificationTitle;
        try {
            notificationTitle = remoteMessage.getNotification().getTitle();
        } catch (Exception e) {
            Log.e(TAG, "showLocalNotification: Title null", e);
            notificationTitle = getString(R.string.app_name);
        }

        String notificationBody;
        try {
            notificationBody = remoteMessage.getNotification().getBody();
        } catch (Exception e) {
            Log.e(TAG, "showLocalNotification: Body null", e);
            notificationBody = "Vehicle Alert";
        }

        Intent resultIntent = new Intent(this, AlertListActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra(REMOTE_MESSAGE_BUNDLE_KEY, remoteMessage);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(AlertListActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(NOTIFICATION_REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                    new NotificationChannel(
                            getString(R.string.default_notification_channel_id),
                            getString(R.string.default_notification_channel_id),
                            NotificationManager.IMPORTANCE_DEFAULT
                    )
            );
        }

        notificationManager.notify(getTimeBasedNotificationID(), notificationBuilder.build());
    }

    private int getTimeBasedNotificationID() {
        try {
            return Integer
                    .parseInt(
                            new SimpleDateFormat("HHmmssSSS", Locale.US)
                                    .format(System.currentTimeMillis())
                    );
        } catch (Exception e) {
            Log.e(TAG, "getTimeBasedNotificationID: ", e);
        }
        return 0;
    }
}