package ba.ctrl.ctrltest1.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * This class starts the Service if not started. If Service is already started only its onStartCommand() is called.
 * Used in AlarmManager and executed periodically while apps Activity is shown on screen!
 * 
 * @author Trax
 *
 */
public class ServicePingerAlarmReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serInt = new Intent(context, CtrlService.class);
        serInt.putExtra(CtrlService.BC_SERVICE_START_METHOD, true);
        // context.startService(serInt);
        startWakefulService(context, serInt);
        // Log.i("ServicePingerAlarmReceiver", "Pinging Service...");
    }

}
