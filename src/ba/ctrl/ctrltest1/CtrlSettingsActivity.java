package ba.ctrl.ctrltest1;

import java.io.File;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.GcmBroadcastReceiver;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class CtrlSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, ServiceStatusReceiverCallbacks {
    private static final String TAG = "CtrlSettingsActivity";

    protected AlertDialog editorDialog;

    private Context context;
    private DataSource dataSource = null;

    private ServiceStatusReceiver serviceStatusReceiver;
    private ActionBar actionBar;

    // For "pinging" the Service to keep it running
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ctrl_settings_activity);

        // Open the database if not already open
        if (dataSource == null) {
            dataSource = DataSource.getInstance(this);
        }

        context = this.getApplicationContext();

        actionBar = getActionBar();
        // Show the Up button in the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferencesEditor = preferences.edit();

        // Create the "cert" directory in external storage
        File myFilesDir = new File(CommonStuff.getCustomSslCertDir(context));
        if (!(myFilesDir.mkdirs() || myFilesDir.isDirectory())) {
            toaster("Failed to create custom SSL Certificate directory!");
        }

        // Add summaries on startup
        setSummaryForEditTextPreference("ctrl_server", dataSource.getPubVar("ctrl_server", CommonStuff.CTRL_DEFAULT_SERVER));
        setSummaryForEditTextPreference("ctrl_server_port", dataSource.getPubVar("ctrl_server_port", String.valueOf(CommonStuff.CTRL_SERVER_DEFAULT_PORT)));
        if (!dataSource.getPubVar("auth_token").equals(""))
            setSummaryForEditTextPreference("auth_token", dataSource.getPubVar("auth_token"));

        // onChange and onClick listeners
        ((EditTextPreference) findPreference("ctrl_server")).setOnPreferenceChangeListener(this);
        ((EditTextPreference) findPreference("ctrl_server_port")).setOnPreferenceChangeListener(this);
        ((EditTextPreference) findPreference("auth_token")).setOnPreferenceChangeListener(this);

        // yep, this is a special case
        ((Preference) findPreference("gcm_rereg")).setOnPreferenceClickListener(this);
        ((Preference) findPreference("auth_token_scan")).setOnPreferenceClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Register required receivers
        IntentFilter filter = new IntentFilter(CtrlService.BC_SERVICE_STATUS);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        serviceStatusReceiver = new ServiceStatusReceiver(this);
        registerReceiver(serviceStatusReceiver, filter);

        // Create AlarmManager to repeatedly "ping" the Service at 1/2 the rate
        // Service expects (because we are using inexact repeating alarm). Do
        // that as long as we are started in foreground. This will initially
        // start the service!
        // Do not ping the Service if there is not AuthToken set!
        if (!dataSource.getPubVar("auth_token").equals("")) {
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, ServicePingerAlarmReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

        super.onStop();
    }

    @SuppressWarnings("deprecation")
    private void setSummaryForEditTextPreference(String key, String value) {
        EditTextPreference editTextPreference = (EditTextPreference) this.findPreference(key);
        editTextPreference.setSummary(value);

        if (!preferences.contains(key)) {
            preferencesEditor.putString(key, value);
            preferencesEditor.apply();
        }
    }

    /*
    @SuppressWarnings("deprecation")
    private void setSummaryForPreference(String key, String value) {
        Preference preference = (Preference) this.findPreference(key);
        preference.setSummary(value);
    }
    */

    @SuppressWarnings("deprecation")
    private void setSummaryForListPreference(String key, String value) {
        ListPreference listPreference = (ListPreference) this.findPreference(key);
        int index = -1;

        if (value != null && !value.equals(""))
            index = listPreference.findIndexOfValue(value);

        listPreference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue == null)
            return true;

        String key = preference.getKey();
        String sNewVal = newValue.toString();

        dataSource.savePubVar(key, sNewVal);

        // if user changed the AuthToken, re-connect
        if ("auth_token".equals(key)) {
            toaster("Success, connecting...");
            actionBar.setSubtitle("Connecting...");
            CommonStuff.serviceTaskRestart(context);
        }

        if (preference instanceof ListPreference) {
            setSummaryForListPreference(key, sNewVal);
        }
        else {
            preference.setSummary(sNewVal);
        }

        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference p) {
        String key = p.getKey();

        if ("auth_token_scan".equals(key)) {
            IntentIntegrator scanIntegrator = new IntentIntegrator(CtrlSettingsActivity.this);
            scanIntegrator.setSingleTargetApplication("com.google.zxing.client.android");
            scanIntegrator.initiateScan();
        }
        else if ("gcm_rereg".equals(key)) {
            CommonStuff.serviceTaskGcmRereg(context);
            toaster("Done.");
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // start the scanner
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        // scanner ended, lets see if we have something as a return
        if (scanningResult != null && scanningResult.getContents() != null) {
            String scanContent = scanningResult.getContents();
            dataSource.savePubVar("auth_token", scanContent);

            preferencesEditor.putString("auth_token", scanContent);
            preferencesEditor.apply();
            setSummaryForEditTextPreference("auth_token", scanContent);

            toaster("Success! Connecting...");
            actionBar.setSubtitle("Connecting...");
            CommonStuff.serviceTaskRestart(context);
        }
    }

    // Show Toast message at the bottom
    private void toaster(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void serviceCtrlErrorWrongAuthToken(Context context, Intent intent) {
        Toast.makeText(getApplicationContext(), "Wrong Auth Token!", Toast.LENGTH_SHORT).show();
    }

}
