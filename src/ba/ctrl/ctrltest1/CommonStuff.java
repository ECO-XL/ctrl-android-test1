package ba.ctrl.ctrltest1;

import ba.ctrl.ctrltest1.service.CtrlService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class CommonStuff {
    private final static String TAG = "CommonStuff";

    public final static int CTRL_NOTIFICATION_ID = 623487642; // whatever?
    
    // default CTRL server
    public final static int CTRL_SERVER_DEFAULT_PORT = 9001;
    public final static String CTRL_DEFAULT_SERVER = "ctrl.ba";

    public final static int NET_NOT_CONNECTED = 0;
    public final static int NET_WIFI = 1;
    public final static int NET_MOBILE = 2;

    public static void serviceRequestStatus(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_REQUEST_CONNECTION_STATUS);
        try {
            context.sendBroadcast(broadcastIntent);
        }
        catch (Exception e) {
            Log.e(TAG, "requestServiceStatus() Error: " + e.getMessage());
        }
    }

    public static void serviceTaskGcmRereg(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_GCM_REREG);
        try {
            context.sendBroadcast(broadcastIntent);
        }
        catch (Exception e) {
            Log.e(TAG, "serviceCommandGcmRereg() Error: " + e.getMessage());
        }
    }

    public static void serviceTaskRestart(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_RESTART_CONNECTION);
        try {
            context.sendBroadcast(broadcastIntent);
        }
        catch (Exception e) {
            Log.e(TAG, "commandServiceRestart() Error: " + e.getMessage());
        }
    }

    public static void serviceTaskClose(Context context) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(CtrlService.BC_SERVICE_TASKS);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_CLOSE_CONNECTION);
        try {
            context.sendBroadcast(broadcastIntent);
        }
        catch (Exception e) {
            Log.e(TAG, "commandServiceClose() Error: " + e.getMessage());
        }
    }

    public static String getCustomSslCertDir(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName() + "/cert";
    }

    public static int getNetConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return NET_WIFI;
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return NET_MOBILE;
        }
        return NET_NOT_CONNECTED;
    }

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
