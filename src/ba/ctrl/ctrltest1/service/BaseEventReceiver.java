package ba.ctrl.ctrltest1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BaseEventReceiver extends BroadcastReceiver {

    private BaseEventReceiverCallbacks callbacks;

    public BaseEventReceiver(BaseEventReceiverCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // if there is no "connected key" this can only be the New Data Arrival
        // event
        if (!intent.hasExtra(CtrlService.BC_BASE_EVENT_CONNECTED_KEY))
            callbacks.baseNewDataArrival(intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY));
        // else, this is a New Connection Status event
        else
            callbacks.baseNewConnectionStatus(intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY), intent.getBooleanExtra(CtrlService.BC_BASE_EVENT_CONNECTED_KEY, false));
    }
}
