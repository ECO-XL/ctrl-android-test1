package ba.ctrl.ctrltest1.bases;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import ba.ctrl.ctrltest1.CommonStuff;
import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.GcmBroadcastReceiver;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;

/*
 * There is a little bug with this Activity. For example, when you change a title of some Base
 * and then go back to change title of another base, you get title of previous Base in popup dialog.
 * This is because each Base Title gets saved to SharedPreferences also and that's where it gets
 * loaded from when user clicks to edit some value. This is the same for Lists (Base Types). 
 * It is just annoying and should be fixed somehow...
 * */
public class BaseSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener, ServiceStatusReceiverCallbacks {
    private static final String TAG = "BaseSettingsActivity";

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

    private Base base;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.base_settings_activity);

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

        String baseId = getIntent().getExtras().getString("baseid");
        base = dataSource.getBase(baseId);
        if (base == null) {
            toaster("Failed to fetch Base from Database");
            finish();
            return; // don't go any further
        }

        // Add summaries on startup
        setSummaryForEditTextPreference("title", base.getTitle());
        setSummaryForListPreference("base_type", String.valueOf(base.getBaseType()));

        // onChange and onClick listeners
        ((EditTextPreference) findPreference("title")).setOnPreferenceChangeListener(this);
        ((ListPreference) findPreference("base_type")).setOnPreferenceChangeListener(this);

        // yep, this is a special case
        ((Preference) findPreference("delete")).setOnPreferenceClickListener(this);
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

        // call this to update ActionBar Subtitle status
        CommonStuff.serviceRequestStatus(context);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        // if user changed the AuthToken, re-connect
        if ("title".equals(key)) {
            base.setTitle(sNewVal);
        }
        else if ("base_type".equals(key)) {
            base.setBaseType(Integer.valueOf(sNewVal));
        }

        dataSource.updateBase(base);

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

        if ("delete".equals(key)) {
            AlertDialog.Builder editor_builder = new AlertDialog.Builder(this);
            editor_builder.setTitle("Are you sure?");
            editor_builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dataSource.deleteBase(base.getBaseid());
                    finish();
                }
            });
            editor_builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            editorDialog = editor_builder.create();
            editorDialog.show();
            return true;
        }

        return false;
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
