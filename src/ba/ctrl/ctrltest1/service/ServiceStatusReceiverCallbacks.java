package ba.ctrl.ctrltest1.service;

import android.content.Context;
import android.content.Intent;

public interface ServiceStatusReceiverCallbacks {
    // Service System Status
    public void serviceConnectionError(Context context, Intent intent);
    public void serviceConnectionIdle(Context context, Intent intent);
    public void serviceConnectionRunning(Context context, Intent intent);
    
    // CTRL Errors
    public void serviceCtrlErrorTooManyAuthAttempts(Context context, Intent intent);
    public void serviceCtrlErrorWrongAuthToken(Context context, Intent intent);
}
