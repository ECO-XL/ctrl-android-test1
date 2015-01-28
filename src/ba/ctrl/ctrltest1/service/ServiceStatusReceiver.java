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
        if (intent.hasExtra(CtrlService.BC_CONNECTION_STATUS_KEY)) {
            if (intent.getStringExtra(CtrlService.BC_CONNECTION_STATUS_KEY).equals(CtrlService.BC_CONNECTION_STATUS_ERROR)) {
                callbacks.serviceConnectionError(context, intent);
            }
            else if (intent.getStringExtra(CtrlService.BC_CONNECTION_STATUS_KEY).equals(CtrlService.BC_CONNECTION_STATUS_IDLE)) {
                callbacks.serviceConnectionIdle(context, intent);
            }
            else if (intent.getStringExtra(CtrlService.BC_CONNECTION_STATUS_KEY).equals(CtrlService.BC_CONNECTION_STATUS_RUNNING)) {
                callbacks.serviceConnectionRunning(context, intent);
            }
        }

        // CTRL errors
        if (intent.hasExtra(CtrlService.BC_CTRL_STATUS_KEY)) {
            if (intent.getStringExtra(CtrlService.BC_CTRL_STATUS_KEY).equals(CtrlService.BC_CTRL_STATUS_TOO_MANY)) {
                callbacks.serviceCtrlErrorTooManyAuthAttempts(context, intent);
            }
            else if (intent.getStringExtra(CtrlService.BC_CTRL_STATUS_KEY).equals(CtrlService.BC_CTRL_STATUS_WRONG_AUTH)) {
                callbacks.serviceCtrlErrorWrongAuthToken(context, intent);
            }
        }
    }
}
