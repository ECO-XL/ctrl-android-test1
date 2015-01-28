package ba.ctrl.ctrltest1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

// There is a particular situation with this BroadcastReceiver, actually it is with the
// sender (OS). On some devices this broadcast will be received even though there was no
// change in network connectivity. This is because on *some devices* this broadcast
// is "sticky".
// Read here: http://stackoverflow.com/questions/16427812/broadcastreceiver-onreceive-triggered-when-registered/16428823#16428823
// This situation is handled in CtrlService by simply declaring a boolean variable networkConnected and
// initializing it with the actual connection state in onCreate(). Now when we receive the first broadcast
// we compare it to this boolean and if they mismatch then broadcast is legit. Else, it is a duplicate (sticky) that we ignore.

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";
    // private static boolean firstConnect = true;

    private NetworkStateReceiverCallbacks callbacks;

    public NetworkStateReceiver(NetworkStateReceiverCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "Network connectivity change.");

        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo ni = connectivityManager.getActiveNetworkInfo();

            if (ni != null && ni.isConnectedOrConnecting() && !ni.isConnected()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connecting...");
            }
            else if (ni != null && ni.isConnected()) {
                Log.i(TAG, "Network " + ni.getTypeName() + " connected!");

                // http://stackoverflow.com/questions/8412714/broadcastreceiver-receives-multiple-identical-messages-for-one-event
                // if (firstConnect) {
                callbacks.networkStateChanged(true);
                // firstConnect = false;
                // }
            }
            else if ((ni != null && !ni.isConnected()) || intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d(TAG, "There's no network connectivity whatsoever!");

                callbacks.networkStateChanged(false);
                // firstConnect = true;
            }
        }
    }
}
