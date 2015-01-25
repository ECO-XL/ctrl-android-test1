package ba.ctrl.ctrltest1;

import java.io.File;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import ba.ctrl.ctrltest1.database.DataSource;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class CtrlSettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
    protected AlertDialog editorDialog;

    private Context context;
    private DataSource dataSource = null;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferencesEditor = preferences.edit();

        // Create the "cert" directory in external storage
        File myFilesDir = new File(CommonStuff.getCustomSslCertDir(context));
        if (!(myFilesDir.mkdirs() || myFilesDir.isDirectory())) {
            toaster("Failed to create custom SSL Certificate directory!");
        }

        // Add summaries on startup
        setSummaryForEditTextPreference("ctrl_server", dataSource.getPubVar("ctrl_server", CommonStuff.CTRL_SERVER));
        setSummaryForEditTextPreference("ctrl_server_port", dataSource.getPubVar("ctrl_server_port", String.valueOf(CommonStuff.CTRL_VERSION)));
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

    @SuppressWarnings("deprecation")
    private void setSummaryForEditTextPreference(String key, String value) {
        EditTextPreference editTextPreference = (EditTextPreference) this.findPreference(key);
        editTextPreference.setSummary(value);

        if (!preferences.contains(key)) {
            preferencesEditor.putString(key, value);
            preferencesEditor.apply();
        }
    }

    @SuppressWarnings("deprecation")
    private void setSummaryForPreference(String key, String value) {
        Preference preference = (Preference) this.findPreference(key);
        preference.setSummary(value);
    }

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

            toaster("Success, you can go back now!");
        }
    }

    // Show Toast message at the bottom
    private void toaster(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
