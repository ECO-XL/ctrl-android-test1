package ba.ctrl.ctrltest1.service;

import android.content.Context;
import android.content.Intent;

public interface ServiceStatusReceiverCallbacks {
    // Service System Status
    public void serviceError(Context context, Intent intent);
    public void serviceIdle(Context context, Intent intent);
    public void serviceRunning(Context context, Intent intent);
    
    // CTRL Errors
    public void errorTooManyAuthAttempts(Context context, Intent intent);
    public void errorWrongAuthToken(Context context, Intent intent);
}
