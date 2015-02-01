package ba.ctrl.ctrltest1.service;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "GcmBroadcastReceiver";

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

        // If this is a tickle notification, start the service and let it do
        // it's job
        if ("tickle-tickle".equals(extras.getString("what"))) {
            Intent serInt = new Intent(context, CtrlService.class);
            serInt.putExtra(CtrlService.BC_SERVICE_START_METHOD, false);
            // context.startService(serInt);
            startWakefulService(context, serInt);
        }
        else {
            // If this notification is not about some Base, handle it
            // differently. This is for future features...
            Toast.makeText(context, "SERVICE NOTICE...", Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "GcmBroadcastReceiver.onReceive() finished!");
    }
}
