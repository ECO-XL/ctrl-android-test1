package ba.ctrl.ctrltest1;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class CommonStuff {
    public final static int CTRL_NOTIFICATION_ID = 623487642; // whatever?

    // default CTRL server
    public final static int CTRL_DEFAULT_PORT = 9001;
    public final static String CTRL_DEFAULT_SERVER = "ctrl.ba";

    public final static int NET_NOT_CONNECTED = 0;
    public final static int NET_WIFI = 1;
    public final static int NET_MOBILE = 2;

    // http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // http://stackoverflow.com/questions/4785654/convert-a-string-of-hex-into-ascii-in-java
    public static String hexStringToString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }
        return output.toString();
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
