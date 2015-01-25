package ba.ctrl.ctrltest1;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class CommonStuff {

    // default CTRL server
    public final static int CTRL_VERSION = 9001;
    public final static String CTRL_SERVER = "ctrl.ba";
    
    public final static int NET_NOT_CONNECTED = 0;
    public final static int NET_WIFI = 1;
    public final static int NET_MOBILE = 2;

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
