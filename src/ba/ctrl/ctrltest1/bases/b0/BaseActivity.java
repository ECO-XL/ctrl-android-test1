package ba.ctrl.ctrltest1.bases.b0;

import java.util.ArrayList;

import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.bases.BaseSettingsActivity;
import ba.ctrl.ctrltest1.bases.BaseTemplateActivity;
import ba.ctrl.ctrltest1.service.CtrlService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BaseActivity extends BaseTemplateActivity {
    private Base base;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setLayoutR(R.layout.b0_base_activity);
        super.onCreate(savedInstanceState);

        base = super.getBase();

        context = super.getApplicationContext();

        /* IMPLEMENTATION OF THIS BASE TYPE 0 : */

        final SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);

        ((Button) findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(0);
            }
        });

        ((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(500);
            }
        });

        ((Button) findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setProgress(1000);
            }
        });

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sendCurrentSeekBarValue();
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // If this is a re-creation of previously displayed activity
        if (savedInstanceState != null) {
            super.myLog("onCreate() re-creation of Activity, I should have data in bundle (or db depending on implementation).");
        }
        else {
            super.myLog("onCreate() fresh start");
        }
    }

    private void sendCurrentSeekBarValue() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);

        short val = (short) (seekBar.getProgress() + 1000);

        Log.i("BLABLA", "SEEKBAR: " + val);

        Intent intSer = new Intent();
        intSer.setAction(CtrlService.BC_SERVICE_TASKS);
        intSer.addCategory(Intent.CATEGORY_DEFAULT);
        intSer.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_SEND_DATA);
        intSer.putExtra("isNotification", checkBox.isChecked());
        intSer.putExtra("sendData", String.valueOf(val));
        // lets target just this Base...
        ArrayList<String> baseIds = new ArrayList<String>();
        baseIds.add(base.getBaseid());
        intSer.putExtra("baseIds", baseIds);
        try {
            context.sendBroadcast(intSer);
        }
        catch (Exception e) {
            // ajrror
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.b0_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, BaseSettingsActivity.class);
            intent.putExtra("baseid", base.getBaseid());
            startActivity(intent);
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void baseNewDataArrival(String baseId) {
        super.getDataSource().markBaseDataSeen(base.getBaseid());

        // do sometihng about this new data arrival event
    }

    @Override
    public void baseNewConnectionStatus(String baseId, boolean connected) {

        // do something about this new base status event
    }

}
