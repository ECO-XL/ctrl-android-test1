package ba.ctrl.ctrltest1.service;

import ba.ctrl.ctrltest1.service.CtrlService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ForegroundCheckerReceiver extends BroadcastReceiver {

    private String baseId;
    private boolean answerToConnStateChangedOfAnyBase;

    public ForegroundCheckerReceiver(String baseId, boolean answerToConnStateChangedOfAnyBase) {
        this.baseId = baseId;
        this.answerToConnStateChangedOfAnyBase = answerToConnStateChangedOfAnyBase;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent.getStringExtra(CtrlService.BC_FOREGROUND_CHECKER_BASEID).equals(baseId) || intent.getBooleanExtra(CtrlService.BC_FOREGROUND_CHECKER_CONNSTATECHANGED, false) == answerToConnStateChangedOfAnyBase)) {
            if (this.isOrderedBroadcast()) {
                this.setResultCode(Activity.RESULT_OK);
            }
        }
    }

}
