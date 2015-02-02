package ba.ctrl.ctrltest1.bases;

import ba.ctrl.ctrltest1.CommonStuff;
import ba.ctrl.ctrltest1.CtrlSettingsActivity;
import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.BaseEventReceiver;
import ba.ctrl.ctrltest1.service.BaseEventReceiverCallbacks;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BaseTemplateActivity extends Activity implements ServiceStatusReceiverCallbacks, BaseEventReceiverCallbacks {
    private String TAG = "BaseTemplateActivity";

    private Context context;
    private DataSource dataSource = null;

    private ServiceStatusReceiver serviceStatusReceiver;
    private BaseEventReceiver baseEventReceiver;

    private int rLayoutName;
    private Base base;

    private ActionBar actionBar;

    private int baseType;

    // For "pinging" the Service to keep it running
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(rLayoutName);

        // Open the database if not already open
        if (dataSource == null) {
            dataSource = DataSource.getInstance(this);
        }

        context = this.getApplicationContext();

        // Get parameter from MainActivity
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.e(TAG, "Could't load Activity because Intent Extras is null!");
            Toast.makeText(context, "Couldn't load Activity!", Toast.LENGTH_LONG).show();
            finish();
            return; // don't go any further
        }

        String baseId = extras.getString("baseid");
        base = dataSource.getBase(baseId); // Fetch Base
        if (base == null) {
            Log.e(TAG, "Could't load Activity because Base is not fetched from DB!");
            Toast.makeText(context, "Couldn't load Activity!", Toast.LENGTH_LONG).show();
            finish();
            return; // don't go any further
        }

        baseType = base.getBaseType();

        TAG = TAG + " - " + base.getBaseid();

        actionBar = getActionBar();
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");

        // Register required receivers
        IntentFilter filter = new IntentFilter(CtrlService.BC_SERVICE_STATUS);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        serviceStatusReceiver = new ServiceStatusReceiver(this);
        registerReceiver(serviceStatusReceiver, filter);

        IntentFilter filter2 = new IntentFilter(CtrlService.BC_BASE_EVENT);
        filter2.addCategory(Intent.CATEGORY_DEFAULT);
        baseEventReceiver = new BaseEventReceiver(this);
        registerReceiver(baseEventReceiver, filter2);

        // Create AlarmManager to repeatedly "ping" the Service at 1/2 the rate
        // Service expects (because we are using inexact repeating alarm). Do
        // that as long as we are started in foreground. This will initially
        // start the service!
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServicePingerAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 2000, alarmIntent);
        Log.i(TAG, "AlarmManager set.");
        actionBar.setSubtitle("Connecting...");

        if (CommonStuff.getNetConnectivityStatus(context) == CommonStuff.NET_NOT_CONNECTED) {
            Toast.makeText(context, "No Internet Connectivity!", Toast.LENGTH_LONG).show();
        }

        // call this to update ActionBar Subtitle status
        CommonStuff.serviceRequestStatus(context);

        dataSource.markBaseDataSeen(base.getBaseid());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onRestoreInstanceState()");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i(TAG, "onSaveInstanceState()");
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Base might have been deleted if we came back from Settings, or it
        // might have some data changed. Lets handle that.
        base = dataSource.getBase(base.getBaseid());
        if (base == null) {
            Toast.makeText(context, "Base deleted!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Check if base type has just been changed and if yes, we need to kill
        // this screen
        if (baseType != base.getBaseType()) {
            Toast.makeText(context, "Base Type changed, tap again to open", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Always set title...
        setTitle(base.getTitle());

        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        super.onPause();
    }

    @Override
    public void onStop() {
        // Stop the AlarmManager from "pinging" the Service
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
            Log.i(TAG, "AlarmManager cancelled.");
        }

        // Unregister receivers
        unregisterReceiver(serviceStatusReceiver);
        unregisterReceiver(baseEventReceiver);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        super.onDestroy();
    }

    @Override
    public void serviceConnectionError(Context context, Intent intent) {
        ActionBar actionBar = getActionBar();
        actionBar.setSubtitle("Error!");
    }

    @Override
    public void serviceConnectionIdle(Context context, Intent intent) {
        ActionBar actionBar = getActionBar();
        actionBar.setSubtitle("Disconnected");
    }

    @Override
    public void serviceConnectionRunning(Context context, Intent intent) {
        ActionBar actionBar = getActionBar();
        actionBar.setSubtitle("Connected");

        //NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.cancel(GcmBroadcastReceiver.CTRL_NOTIFICATION_ID);
    }

    @Override
    public void serviceCtrlErrorTooManyAuthAttempts(Context context, Intent intent) {
        Toast.makeText(getApplicationContext(), "Too many authentication requests, verify Auth Token and try later!", Toast.LENGTH_LONG).show();

        Intent settingsIntent = new Intent(context, CtrlSettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void serviceCtrlErrorWrongAuthToken(Context context, Intent intent) {
        Toast.makeText(getApplicationContext(), "Wrong Auth Token!", Toast.LENGTH_SHORT).show();

        Intent settingsIntent = new Intent(context, CtrlSettingsActivity.class);
        startActivity(settingsIntent);
    }

    public Base getBase() {
        return this.base;
    }

    public void reloadBase() {
        base = dataSource.getBase(base.getBaseid());
    }

    public void setLayoutR(int rLayoutName) {
        this.rLayoutName = rLayoutName;
    }

    public void myLog(String msg) {
        Log.i(TAG, msg);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    // Will be overriden by child class
    @Override
    public void baseNewDataArrival(String baseId) {
        // Not implemented here...
    }

    // Will be overriden by child class
    @Override
    public void baseNewConnectionStatus(String baseId, boolean connected) {
        // Not implemented here...
    }
}
