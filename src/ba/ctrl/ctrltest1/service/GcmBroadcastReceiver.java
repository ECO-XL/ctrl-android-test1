package ba.ctrl.ctrltest1.service;

import ba.ctrl.ctrltest1.R;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GcmBroadcastReceiver";

    public static final int CTRL_NOTIFICATION_ID = 1234567890;
    NotificationManager mNotificationManager;

    // for sendBroadcast() to any listening activiti(es)
    public static final String GCMRECEIVER_RESPONSE = "ba.ctrl.ctrltest1.intent.action.GCMRECEIVER_RESPONSE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "GcmBroadcastReceiver.onReceive() started...");

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        String messageType = gcm.getMessageType(intent);

        Bundle extras = intent.getExtras();

        // if this is not a real message, ie. it might be a notification that
        // server deleted older messages or it might be a "send" error. we
        // aren't sending anything so we don't care, and if server deleted - we
        // don't care also
        if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            Log.d(TAG, "GcmBroadcastReceiver.onReceive() ignoring notification because it is not a message...");
            return;
        }

        // if extras are not available, something wierd happened
        if (extras.isEmpty()) {
            Log.e(TAG, "GcmBroadcastReceiver.onReceive() error because Intent Extras is empty!");
            return;
        }

        Log.i(TAG, "GcmBroadcastReceiver.onReceive():" + extras);

        String title = "";
        String content = "";
        if ("onBaseStatusChange".equals(extras.getString("why"))) {
            title = "Base Status";
            if ("true".equals(extras.getString("connected")))
                content = "Base is now online!";
            else
                content = "Base just went offline.";
        }
        else if ("onBaseMessage".equals(extras.getString("why"))) {
            title = "Base Message";
            content = "Service inserts into DB, and calls class to parse and alert if required...";
        }

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        showNotification(context, "CTRL", title, content);

        /*
                // start a service
                Intent intentForService = new Intent(context, CtrlRemoteService.class);
                intentForService.putExtras(extras); // forward extras we received from GCM to Service
                intentForService.putExtra(CtrlRemoteService.IN_KEY_TASK_ACTION, CtrlRemoteService.VAL_TASK_ACTION_GCMRECEIVER); // tell service it was started by GCM
                intentForService.putExtra(CtrlRemoteService.BY_ACTIVITY, ""); // started by no activity
                Log.d(LOG_PREFIX, "GcmBroadcastReceiver.onReceive() starting Service...");
                try
                {
                    startWakefulService(context, intentForService);
                }
                catch (Exception e)
                {
                    Log.e(LOG_PREFIX, "GcmBroadcastReceiver.onReceive() error during starting IntentService. Error: " + e.getMessage());
                }
        */

        Log.d(TAG, "GcmBroadcastReceiver.onReceive() finished!");
    }

    // TEST
    private void showNotification(Context context, String ticker, String title, String content) {
        // This is who should be launched if the user selects our notification.
        Intent contentIntent = new Intent();

        Notification n = new Notification(R.drawable.ic_launcher, ticker, System.currentTimeMillis());
        PendingIntent appIntent = PendingIntent.getActivity(context, 0, contentIntent, 0);
        n.setLatestEventInfo(context, title, content, appIntent);

        mNotificationManager.notify(CTRL_NOTIFICATION_ID, n);
    }
}
