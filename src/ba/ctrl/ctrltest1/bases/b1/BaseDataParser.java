package ba.ctrl.ctrltest1.bases.b1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import ba.ctrl.ctrltest1.CommonStuff;
import ba.ctrl.ctrltest1.R;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.bases.BaseDataParserInterface;
import ba.ctrl.ctrltest1.database.DataSource;

public class BaseDataParser implements BaseDataParserInterface {
    private DataSource dataSource = null;

    @Override
    public void doParse(Context context, String baseId, String data, boolean connectedStateChanged, boolean connected) {
        if (dataSource == null) {
            dataSource = DataSource.getInstance(context);
        }

        Base b = dataSource.getBase(baseId);

        // Here we can process recevied data, or we can even call database to
        // get all received data for this baseId...
        // We can check if something important happened like if temperature is
        // above some preset value and we can show a notification and play alarm
        // sound.

        // Custom stuff here...

        // If this is an online/offline event (connected state changed)
        if (data == null) {
            if (connectedStateChanged) {
                if (connected)
                    showNotification(context, baseId, b.getTitle() + " is now Online", "Base is now Online! Tap to interact.", true);
                else
                    showNotification(context, baseId, b.getTitle() + " is now Offline", "Base went Offline.", true);
            }
        }
        // Nope, this is some new data arrived
        else {
            showNotification(context, baseId, b.getTitle() + " rotated", "New position: " + Misc.dataToStringDegrees(data) + " degrees.", true);
        }
    }

    private void showNotification(Context context, String baseId, String contentTitle, String contentText, boolean sound) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_stat_logo);
        mBuilder.setContentTitle(contentTitle);
        mBuilder.setContentText(contentText);

        if (sound) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
        }

        mBuilder.setLights(Color.RED, 500, 500);

        String cTargetName = context.getPackageName() + ".bases.b1.BaseActivity";
        Class<?> cTarget = null;
        try {
            cTarget = Class.forName(cTargetName);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, cTarget);
        resultIntent.putExtra("baseid", baseId);

        // The stack builder object will contain an artificial back stack for
        // the started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        String cMainName = context.getPackageName() + ".MainActivity";
        Class<?> cMain = null;
        try {
            cMain = Class.forName(cMainName);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(cMain);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(baseId, CommonStuff.CTRL_NOTIFICATION_ID, mBuilder.build());
    }

}
