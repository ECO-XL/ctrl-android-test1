package ba.ctrl.ctrltest1;

import ba.ctrl.ctrltest1.adapters.BaseListAdapter;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.BaseEventReceiver;
import ba.ctrl.ctrltest1.service.BaseEventReceiverCallbacks;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import ba.ctrl.ctrltest1.R;

public class MainActivity extends ListActivity implements ServiceStatusReceiverCallbacks, BaseEventReceiverCallbacks {
    private final static int REQ_CODE_PLAY_SERVICES_RESOLUTION = 9000;
    private static final String TAG = "MainActivity";

    private Context context;
    private DataSource dataSource = null;

    private BaseListAdapter adapter = null;

    private ServiceStatusReceiver serviceStatusReceiver;
    private BaseEventReceiver baseEventReceiver;

    private ActionBar actionBar;

    // For "pinging" the Service to keep it running
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Open the database if not already open
        if (dataSource == null) {
            dataSource = DataSource.getInstance(this);
        }

        context = this.getApplicationContext();

        adapter = null; // treba da bi onStart() inicijalno ucitao podatke
                        // onCreate() -> onStart() -> onRestoreInstanceState()
                        // -> onResume()

        actionBar = getActionBar();

        // Setuj onClick listener na listveiew
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                Base b = (Base) adapter.getItem(position);
                startBaseActivity(b);
            }
        });
    }

    private void startBaseActivity(Base b) {
        String cname = getApplicationContext().getPackageName() + ".bases.b" + b.getBaseType() + ".BaseActivity";
        Class<?> c = null;
        if (cname != null) {
            try {
                c = Class.forName(cname);

                // open it
                Intent intent = new Intent(MainActivity.this, c);
                intent.putExtra("baseid", b.getBaseid());

                startActivity(intent);
            }
            catch (ClassNotFoundException e) {
                Toast.makeText(this, "Not implemented!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "getListView().onItemClick() Module Class (" + cname + ") not found: " + e.getMessage());
            }
            catch (Exception e) {
                Toast.makeText(this, "Can not start Activity :(", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "getListView().onItemClick() Error: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        // Do not ping the Service if AuthToken is not set!
        if (dataSource.getPubVar("auth_token").equals("")) {
            Toast.makeText(context, "Please set Auth Token to continue!", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, CtrlSettingsActivity.class);
            startActivity(intent);
        }
        else {
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ServicePingerAlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 2000, alarmIntent);
            Log.i(TAG, "AlarmManager set.");
            actionBar.setSubtitle("Connecting...");

            if (CommonStuff.getNetConnectivityStatus(context) == CommonStuff.NET_NOT_CONNECTED) {
                actionBar.setSubtitle("Disconnected");
                Toast.makeText(context, "No Internet Connectivity!", Toast.LENGTH_LONG).show();
            }
        }

        // call this to update ActionBar Subtitle status
        CommonStuff.serviceRequestStatus(context);

        // kreiraj ako ga nema, ili samo updejtaj sa novim podacima ako ga ima
        if (adapter == null) {
            Log.i(TAG, "onStart() kreiram adapter jer je null");
            adapter = new BaseListAdapter(this, dataSource.getAllBases());
            setListAdapter(adapter);
        }
        else {
            Log.i(TAG, "onStart() ne kreiram adapter jer ga imam, samo refresham");
            refreshListView();
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // we always need to check this here -
        // http://developer.android.com/google/gcm/client.html#app
        checkPlayServices();
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
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ctrl_settings) {
            Intent intent = new Intent(this, CtrlSettingsActivity.class);
            startActivity(intent);
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    // Refresh ListView by reloading data from scratch
    private void refreshListView() {
        adapter.refill(dataSource.getAllBases());
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, REQ_CODE_PLAY_SERVICES_RESOLUTION).show();
            }
            else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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

    @Override
    public void baseNewDataArrival(String baseId) {
        refreshListView();
    }

    @Override
    public void baseNewConnectionStatus(String baseId, boolean connected) {
        refreshListView();
    }

}
