package ba.ctrl.ctrltest1.bases.b0;

import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.bases.BaseTemplateActivity;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;

public class BaseActivity extends BaseTemplateActivity {
    private Base base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.setLayoutR(R.layout.b0_base_activity);
        super.onCreate(savedInstanceState);

        base = super.getBase();

        /* IMPLEMENTATION OF THIS BASE TYPE 0 : */

        // TODO: custom stuff here...

        /*// send queued data
        ((Button) findViewById(R.id.btnSendSomething)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tell service we are stopping
                Intent intSer = new Intent();
                intSer.setAction(CtrlService.BC_SERVICE_TASKS);
                intSer.addCategory(Intent.CATEGORY_DEFAULT);
                intSer.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_SEND_DATA);
                intSer.putExtra("sendData", "ABCDEF01020304");
                try {
                    context.sendBroadcast(intSer);
                }
                catch (Exception e) {
                    // ajrror
                }
            }
        });

        // send notification data
        ((Button) findViewById(R.id.btnSendSomethingElse)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tell service we are stopping
                Intent intSer = new Intent();
                intSer.setAction(CtrlService.BC_SERVICE_TASKS);
                intSer.addCategory(Intent.CATEGORY_DEFAULT);
                intSer.putExtra(CtrlService.BC_SERVICE_TASKS_KEY, CtrlService.BC_SERVICE_TASKS_SEND_DATA);
                intSer.putExtra("isNotification", true);
                intSer.putExtra("sendData", "010203040506");
                try {
                    context.sendBroadcast(intSer);
                }
                catch (Exception e) {
                    // ajrror
                }
            }
        });*/

        // If this is a re-creation of previously displayed activity
        if (savedInstanceState != null) {
            super.myLog("onCreate() re-creation of Activity, I should have data in bundle (or db depending on implementation).");
        }
        else {
            super.myLog("onCreate() fresh start");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.b0_activity, menu);
        return true;
    }

    /*
    @Override
    public void serviceError(Context context, Intent intent) {
        super.serviceError(context, intent);
    }

    @Override
    public void serviceIdle(Context context, Intent intent) {
        super.serviceIdle(context, intent);
    }

    @Override
    public void serviceRunning(Context context, Intent intent) {
        super.serviceRunning(context, intent);
    }

    @Override
    public void errorTooManyAuthAttempts(Context context, Intent intent) {
        super.errorTooManyAuthAttempts(context, intent);
    }

    @Override
    public void errorWrongAuthToken(Context context, Intent intent) {
        super.errorWrongAuthToken(context, intent);
    }
    */

    @Override
    public void newDataArrivalEvent(Context context, Intent intent, String baseId) {
        super.newDataArrivalEvent(context, intent, baseId);

        // do sometihng about this new data arrival event
    }

    @Override
    public void newBaseStatusEvent(String baseId, boolean connected) {
        super.newBaseStatusEvent(baseId, connected);

        // do something about this new base status event
    }

}
