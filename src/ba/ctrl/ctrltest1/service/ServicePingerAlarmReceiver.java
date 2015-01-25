package ba.ctrl.ctrltest1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This class starts the Service if not started. If Service is already started only its onStartCommand() is called.
 * Used in AlarmManager and executed periodically while apps Activity is shown on screen!
 * 
 * @author Trax
 *
 */
public class ServicePingerAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serInt = new Intent(context, CtrlService.class);
        context.startService(serInt);

        Log.i("ServicePingerAlarmReceiver", "Pinging Service...");
    }

}
