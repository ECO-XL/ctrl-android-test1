package ba.ctrl.ctrltest1.service;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class is used to send requests of any kind to CtrlService.java Service from any other piece of app (usually Activity).
 * It also provides a method of returning a boolean to inform caller of whether Service received this task or not by using callback
 * from ContacterResponse interface. 
 */
public class CtrlServiceContacter {
    private final static String TAG = "CtrlServiceContacter";

    /**
     * Interface for callback...
     */
    public static interface ContacterResponse {
        public void onResponse(boolean serviceReceived);
    }

    /**
     * Send Ordered Broadcast to Service and call callback function if defined to let
     * caller know if Service heard this task or was missed because Service was down.
     * 
     * @param context
     * @param broadcastIntent
     * @param contacterResponse
     */
    private static void sob(Context context, Intent broadcastIntent, final ContacterResponse contacterResponse) {
        context.sendOrderedBroadcast(broadcastIntent, null, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int result = getResultCode();
                Log.i(TAG, "Service Response = " + result);
                if (contacterResponse != null)
                    contacterResponse.onResponse(result != Activity.RESULT_CANCELED);
            }
        }, null, Activity.RESULT_CANCELED, null, null);
    }

    /**
     * Sending data to remote CTRL Base(s). Data is always sent as String. It will be converted to hexadecimal value based on US-ASCII charset.
     * 
     * @param context
     * @param hexData
     * @param isNotification
     * @param baseIds
     * @param contacterResponse
     */
    public static void taskSendData(Context context, String hexData, boolean isNotification, ArrayList<String> baseIds, ContacterResponse contacterResponse) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_SEND_DATA);
        broadcastIntent.putExtra("isNotification", isNotification);
        broadcastIntent.putExtra("sendData", hexData);
        if (baseIds != null)
            broadcastIntent.putExtra("baseIds", baseIds);

        sob(context, broadcastIntent, contacterResponse);
    }

    /**
     * Requests status of Service. It will arrive via another BroadcastIntent from Service.
     * 
     * @param context
     * @param contacterResponse
     */
    public static void taskRequestServiceStatus(Context context, ContacterResponse contacterResponse) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_REQUEST_CONNECTION_STATUS);

        sob(context, broadcastIntent, contacterResponse);
    }

    /**
     * Sends Android GCM Notification REGID to Server Extension for registration.
     * 
     * @param context
     * @param contacterResponse
     */
    public static void taskGcmRereg(Context context, ContacterResponse contacterResponse) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_GCM_REREG);

        sob(context, broadcastIntent, contacterResponse);
    }

    /**
     * Restarts socket connection on Service.
     *  
     * @param context
     * @param contacterResponse
     */
    public static void taskRestart(Context context, ContacterResponse contacterResponse) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_RESTART_CONNECTION);

        sob(context, broadcastIntent, contacterResponse);
    }

    /**
     * Closes socket connection on Service.
     * 
     * @param context
     * @param contacterResponse
     */
    public static void taskClose(Context context, ContacterResponse contacterResponse) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_CLOSE_CONNECTION);

        sob(context, broadcastIntent, contacterResponse);
    }
}
