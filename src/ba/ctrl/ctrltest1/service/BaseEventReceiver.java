package ba.ctrl.ctrltest1.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BaseEventReceiver extends BroadcastReceiver {

    private BaseEventReceiverCallbacks callbacks;
    private String baseId;

    public BaseEventReceiver(BaseEventReceiverCallbacks callbacks, String baseId) {
        this.callbacks = callbacks;
        this.baseId = baseId;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // If there is no "connected key" this can only be the New Data Arrival
        // event
        // If we were instantiated with baseId == null, it means we are
        // listening to ANY base event, else, we catch only those matching our
        // baseId
        if (!intent.hasExtra(CtrlService.BC_BASE_EVENT_CONNECTED_KEY) && (baseId == null || intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY).equals(baseId)))
            callbacks.baseNewDataArrival(intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY), intent.getStringExtra(CtrlService.BC_BASE_EVENT_DATA_KEY));
        // else, this is a New Connection Status event
        else if (baseId == null || intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY).equals(baseId))
            callbacks.baseNewConnectionStatus(intent.getStringExtra(CtrlService.BC_BASE_EVENT_BASE_ID_KEY), intent.getBooleanExtra(CtrlService.BC_BASE_EVENT_CONNECTED_KEY, false));
    }
}
