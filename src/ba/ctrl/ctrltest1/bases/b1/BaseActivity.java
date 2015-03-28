package ba.ctrl.ctrltest1.bases.b1;

import java.util.ArrayList;

import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.bases.BaseSettingsActivity;
import ba.ctrl.ctrltest1.bases.BaseTemplateActivity;
import ba.ctrl.ctrltest1.service.CtrlServiceContacter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BaseActivity extends BaseTemplateActivity {
    private static final String TAG = "b1.BaseActivity";

    private Base base;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setLayoutR(R.layout.b1_base_activity);
        super.onCreate(savedInstanceState);

        base = super.getBase();

        context = super.getApplicationContext();

        /* IMPLEMENTATION OF THIS BASE TYPE 1 : */

        if (!"".equals(super.getVoiceCommand())) {
            // We have voice command, lets see which one it is, and to execute
            // the appropriate action.
            // We have: left, right and center commands.

            String key = "voicecommand_" + base.getBaseid() + "_" + super.getVoiceCommand();
            String voiceCommandAction = super.getSharedPref().getString(key, "");

            if (voiceCommandAction.equals("left")) {
                super.doSpeak("Turning left");
                ((Button) findViewById(R.id.btnSend)).performClick();
            }
            else if (voiceCommandAction.equals("center")) {
                super.doSpeak("Centering");
                ((Button) findViewById(R.id.button2)).performClick();
            }
            else if (voiceCommandAction.equals("right")) {
                super.doSpeak("Turning right");
                ((Button) findViewById(R.id.button3)).performClick();
            }
            else {
                super.doSpeak("Bad command: " + voiceCommandAction + "!");
            }
        }

        ((Button) findViewById(R.id.btnSend)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
                seekBar.setProgress(0);
                sendCurrentSeekBarValue();
            }
        });

        ((Button) findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
                seekBar.setProgress(500);
                sendCurrentSeekBarValue();
            }
        });

        ((Button) findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
                seekBar.setProgress(1000);
                sendCurrentSeekBarValue();
            }
        });

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                sendCurrentSeekBarValue();
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

    @Override
    protected void onResume() {
        super.onResume();

        String data = super.getDataSource().getLatestBaseData(base.getBaseid());
        if (data.equals(""))
            return;

        TextView tvDegrees = (TextView) findViewById(R.id.tvDegrees);
        tvDegrees.setText(Misc.dataToStringDegrees(data) + " degrees");

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setProgress(Misc.angleToProgressValue(Misc.dataToDoubleDegrees(data)));
    }

    private void sendCurrentSeekBarValue() {
        CheckBox checkBox = (CheckBox) findViewById(R.id.ckSendAsNotification);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        short val = (short) (seekBar.getProgress() + 1000);

        // lets target just this Base...
        ArrayList<String> baseIds = new ArrayList<String>();
        baseIds.add(base.getBaseid());

        // Send it man!
        CtrlServiceContacter.taskSendData(context, String.valueOf(val), checkBox.isChecked(), baseIds, new CtrlServiceContacter.ContacterResponse() {
            @Override
            public void onResponse(boolean serviceReceived) {
                if (!serviceReceived) {
                    Toast.makeText(context, "Service didn't respond, try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.b1_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, BaseSettingsActivity.class);
            intent.putExtra("baseid", base.getBaseid());
            startActivity(intent);
        }
        else if (item.getItemId() == R.id.action_learn_voice_commands) {
            // add defaults for now...
            super.getSharedPrefEditor().putString("voicecommand_turn left", base.getBaseid());
            super.getSharedPrefEditor().putString("voicecommand_" + base.getBaseid() + "_turn left", "left");
            
            super.getSharedPrefEditor().putString("voicecommand_center", base.getBaseid());
            super.getSharedPrefEditor().putString("voicecommand_" + base.getBaseid() + "_turn left", "left");
            
            super.getSharedPrefEditor().putString("voicecommand_turn right", base.getBaseid());
            super.getSharedPrefEditor().putString("voicecommand_" + base.getBaseid() + "_turn left", "left");

            super.getSharedPrefEditor().apply();
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void baseNewDataArrival(String baseId, String data) {
        super.baseNewDataArrival(baseId, data);

        super.getDataSource().markBaseDataSeen(base.getBaseid());

        // do sometihng about this new data arrival event

        TextView tvDegrees = (TextView) findViewById(R.id.tvDegrees);
        tvDegrees.setText(Misc.dataToStringDegrees(data) + " degrees");

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        seekBar.setProgress(Misc.angleToProgressValue(Misc.dataToDoubleDegrees(data)));
    }

    @Override
    public void baseNewConnectionStatus(String baseId, boolean connected) {
        super.baseNewConnectionStatus(baseId, connected);

        // do something about this new base status event
    }

}
