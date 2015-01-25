package ba.ctrl.ctrltest1.bases;

import ba.ctrl.ctrltest1.CommonStuff;
import ba.ctrl.ctrltest1.CtrlSettingsActivity;
import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BaseTemplateActivity extends Activity implements ServiceStatusReceiverCallbacks {
    private String TAG = "BaseTemplateActivity";

    private Context context;
    private DataSource dataSource = null;

    private ServiceStatusReceiver serviceStatusReceiver;
    private NewDataArrivalReceiver newDataArrivalReceiver;
    private BaseStatusReceiver baseStatusReceiver;

    private int rLayoutName;
    private Base base;

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
            finish(); // don't go any further
        }

        String baseId = extras.getString("baseid");
        base = dataSource.getBase(baseId); // Fetch Base

        if (base == null) {
            Log.e(TAG, "Could't load Activity because Base is not fetched from DB!");
            Toast.makeText(context, "Couldn't load Activity!", Toast.LENGTH_LONG).show();
            finish(); // don't go any further
        }

        TAG = TAG + " - " + base.getBaseid();

        setTitle(base.getTitle());
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

        IntentFilter filter2 = new IntentFilter(CtrlService.BC_NEW_DATA);
        filter2.addCategory(Intent.CATEGORY_DEFAULT);
        newDataArrivalReceiver = new NewDataArrivalReceiver();
        registerReceiver(newDataArrivalReceiver, filter2);

        IntentFilter filter3 = new IntentFilter(CtrlService.BC_BASE_STATUS);
        filter3.addCategory(Intent.CATEGORY_DEFAULT);
        baseStatusReceiver = new BaseStatusReceiver();
        registerReceiver(baseStatusReceiver, filter3);

        // Create AlarmManager to repeatedly "ping" the Service at 1/2 the rate
        // Service expects (because we are using inexact repeating alarm). Do
        // that as long as we are started in foreground. This will initially
        // start the service!
        // Do not ping the Service if there is no Internet connection!
        if (CommonStuff.getNetConnectivityStatus(context) != CommonStuff.NET_NOT_CONNECTED) {
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ServicePingerAlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 2000, alarmIntent);
            Log.i(TAG, "AlarmManager set.");
        }
        else {
            Toast.makeText(context, "No Internet Connectivity. Disconnected!", Toast.LENGTH_LONG).show();
        }

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
        unregisterReceiver(newDataArrivalReceiver);
        unregisterReceiver(baseStatusReceiver);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        super.onDestroy();
    }

    public class NewDataArrivalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(CtrlService.BC_NEW_DATA_BASE_ID_KEY)) {
                String baseId = intent.getStringExtra(CtrlService.BC_NEW_DATA_BASE_ID_KEY);
                newDataArrivalEvent(context, intent, baseId);
            }
        }
    }

    // Will be overriden by child class
    public void newDataArrivalEvent(Context context, Intent intent, String baseId) {
        dataSource.markBaseDataSeen(base.getBaseid());

        // The rest is not implemented here...
    }

    public class BaseStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(CtrlService.BC_BASE_STATUS_BASE_ID_KEY) && intent.hasExtra(CtrlService.BC_BASE_STATUS_CONNECTED_KEY)) {
                String baseId = intent.getStringExtra(CtrlService.BC_BASE_STATUS_BASE_ID_KEY);
                boolean connected = intent.getBooleanExtra(CtrlService.BC_BASE_STATUS_CONNECTED_KEY, false);
                newBaseStatusEvent(baseId, connected);
            }
        }
    }

    // Will be overriden by child class
    public void newBaseStatusEvent(String baseId, boolean connected) {
        // Not implemented here...
    }

    @Override
    public void serviceError(Context context, Intent intent) {
        Toast.makeText(context, "Error, disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void serviceIdle(Context context, Intent intent) {
        Toast.makeText(context, "Idle, disconnected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void serviceRunning(Context context, Intent intent) {
        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void errorTooManyAuthAttempts(Context context, Intent intent) {
        Toast.makeText(getApplicationContext(), "Too many authentication requests, change Auth Token and try later!", Toast.LENGTH_LONG).show();

        Intent settingsIntent = new Intent(context, CtrlSettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void errorWrongAuthToken(Context context, Intent intent) {
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
}
