package ba.ctrl.ctrltest1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ServiceStatusReceiver extends BroadcastReceiver {
    private ServiceStatusReceiverCallbacks callbacks;

    public ServiceStatusReceiver(ServiceStatusReceiverCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Service system status
        if (intent.getStringExtra(CtrlService.BC_SERVICE_STATUS_SYSTEM_KEY).equals(CtrlService.BC_SERVICE_STATUS_SYSTEM_ERROR)) {
            callbacks.serviceError(context, intent);
        }
        else if (intent.getStringExtra(CtrlService.BC_SERVICE_STATUS_SYSTEM_KEY).equals(CtrlService.BC_SERVICE_STATUS_SYSTEM_IDLE)) {
            callbacks.serviceIdle(context, intent);
        }
        else if (intent.getStringExtra(CtrlService.BC_SERVICE_STATUS_SYSTEM_KEY).equals(CtrlService.BC_SERVICE_STATUS_SYSTEM_RUNNING)) {
            callbacks.serviceRunning(context, intent);
        }

        // CTRL errors
        if (intent.getStringExtra(CtrlService.BC_SERVICE_STATUS_CTRL_ERROR_KEY).equals(CtrlService.BC_SERVICE_STATUS_CTRL_ERROR_TOO_MANY)) {
            callbacks.errorTooManyAuthAttempts(context, intent);
        }
        else if (intent.getStringExtra(CtrlService.BC_SERVICE_STATUS_CTRL_ERROR_KEY).equals(CtrlService.BC_SERVICE_STATUS_CTRL_ERROR_WRONG_AUTH)) {
            callbacks.errorWrongAuthToken(context, intent);
        }
    }
}
