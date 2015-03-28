package ba.ctrl.ctrltest1.bases;

import java.util.ArrayList;
import java.util.Locale;

import ba.ctrl.ctrltest1.CommonStuff;
import ba.ctrl.ctrltest1.CtrlSettingsActivity;
import ba.ctrl.ctrltest1.database.DataSource;
import ba.ctrl.ctrltest1.service.BaseEventReceiver;
import ba.ctrl.ctrltest1.service.BaseEventReceiverCallbacks;
import ba.ctrl.ctrltest1.service.CtrlService;
import ba.ctrl.ctrltest1.service.CtrlServiceContacter;
import ba.ctrl.ctrltest1.service.ForegroundCheckerReceiver;
import ba.ctrl.ctrltest1.service.ServicePingerAlarmReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiver;
import ba.ctrl.ctrltest1.service.ServiceStatusReceiverCallbacks;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

public class BaseTemplateActivity extends Activity implements ServiceStatusReceiverCallbacks, BaseEventReceiverCallbacks, TextToSpeech.OnInitListener {
    private String TAG = "BaseTemplateActivity";

    private Context context;
    private DataSource dataSource = null;

    private ServiceStatusReceiver serviceStatusReceiver;
    private BaseEventReceiver baseEventReceiver;
    private ForegroundCheckerReceiver foregroundCheckerReceiver;

    private int rLayoutName;
    private Base base;

    private ActionBar actionBar;

    private int baseType;

    private String ctrlConnectedSubtitle;
    private boolean baseConnected = true;

    private String voiceCommand;

    private TextToSpeech tts;
    private boolean ttsIsReady = false;
    private ArrayList<String> speakOutQueue;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor sharedPrefEditor;

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

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPref.edit();

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

        voiceCommand = extras.getString("voiceCommand", "");

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
        baseEventReceiver = new BaseEventReceiver(this, base.getBaseid());
        registerReceiver(baseEventReceiver, filter2);

        IntentFilter filter3 = new IntentFilter(CtrlService.BC_FOREGROUND_CHECKER);
        filter3.addCategory(Intent.CATEGORY_DEFAULT);
        foregroundCheckerReceiver = new ForegroundCheckerReceiver(base.getBaseid(), false);
        registerReceiver(foregroundCheckerReceiver, filter3);

        // Create AlarmManager to repeatedly "ping" the Service at 1/2 the rate
        // Service expects (because we are using inexact repeating alarm). Do
        // that as long as we are started in foreground. This will initially
        // start the service!
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ServicePingerAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmMgr.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), 2000, alarmIntent);
        Log.i(TAG, "AlarmManager set.");

        ctrlConnectedSubtitle = "Connecting...";
        baseConnected = base.isConnected();
        updateSubtitle();

        if (CommonStuff.getNetConnectivityStatus(context) == CommonStuff.NET_NOT_CONNECTED) {
            Toast.makeText(context, "No Internet Connectivity!", Toast.LENGTH_LONG).show();
        }

        // call this to update ActionBar Subtitle status
        CtrlServiceContacter.taskRequestServiceStatus(context, null);

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
        unregisterReceiver(foregroundCheckerReceiver);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");

        // Don't forget to shutdown tts!
        if(tts != null)
        {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }

    @Override
    public void serviceConnectionError(Context context, Intent intent) {
        ctrlConnectedSubtitle = "Error!";
        updateSubtitle();
    }

    @Override
    public void serviceConnectionIdle(Context context, Intent intent) {
        ctrlConnectedSubtitle = "Disconnected";
        updateSubtitle();
    }

    @Override
    public void serviceConnectionRunning(Context context, Intent intent) {
        ctrlConnectedSubtitle = "Connected";
        updateSubtitle();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(base.getBaseid(), CommonStuff.CTRL_NOTIFICATION_ID);
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

    public String getVoiceCommand() {
        return this.voiceCommand;
    }

    public Base getBase() {
        return this.base;
    }

    public boolean isBaseConnected() {
        return this.baseConnected;
    }

    public void reloadBase() {
        base = dataSource.getBase(base.getBaseid());
    }

    public SharedPreferences getSharedPref() {
        return this.sharedPref;
    }
    
    public SharedPreferences.Editor getSharedPrefEditor() {
        return this.sharedPrefEditor;
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
    public void baseNewDataArrival(String baseId, String data) {
        // Not implemented here...
    }

    // Will be overriden by child class
    @Override
    public void baseNewConnectionStatus(String baseId, boolean connected) {
        baseConnected = connected;
        updateSubtitle();

        // The rest is not implemented here...
    }

    private void updateSubtitle() {
        String baseConnText = "Offline";
        if (baseConnected)
            baseConnText = "Online";

        actionBar.setSubtitle(ctrlConnectedSubtitle + ", " + baseConnText);
    }

    /* TEXT TO SPEECH INIT LISTENER */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setPitch((float) 1);
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "SPEACH: This Language is not supported");
            }
            else {
                ttsIsReady = true;

                // speak all we have in our own queue
                for (int i = 0; i < speakOutQueue.size(); i++) {
                    doSpeak(speakOutQueue.get(i));
                }
                speakOutQueue.clear();
            }
        }
        else {
            Log.e(TAG, "SPEACH: Initilization Failed!");
        }
    }

    public void doSpeak(String msg) {
        if (!ttsIsReady) {
            speakOutQueue.add(msg);
        }
        else {
            tts.speak(msg, TextToSpeech.QUEUE_ADD, null);
        }
    }

}
