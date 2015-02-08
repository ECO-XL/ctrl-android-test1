package ba.ctrl.ctrltest1.database;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import ba.ctrl.ctrltest1.bases.Base;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSource {
    private SQLiteDatabase db = null;
    private DatabaseHandler handler = null;
    private static DataSource instance = null;
    private int useCounter = 0;

    private DataSource() {
    }

    private DataSource(Context context) {
        if (handler == null)
            handler = new DatabaseHandler(context.getApplicationContext());
    }

    /**
     * Call this to get access to the instance of CtrlDataSource Singleton
     */
    public static synchronized DataSource getInstance(Context context) {
        if (instance == null)
            instance = new DataSource(context);
        return instance;
    }

    private void open() throws SQLException {
        useCounter++;

        db = handler.getWritableDatabase();
    }

    private void close() {
        if (useCounter > 0) {
            useCounter--;
        }

        // this makes it thread-safe the stupid-way, but it kind of works on my
        // phone!
        // TODO UPDATE: this doesn't work great, I still get Database Locked
        // errors
        if (useCounter <= 0) {
            handler.close();
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Clone is not allowed.");
    }

    // Database related functions
    // //////////////////////////

    // Get one Base
    public Base getBase(String baseId) {
        Base base = null;

        open();
        Cursor cursor = db.rawQuery("SELECT baseid, base_type, COALESCE(title,''), connected, stamp FROM base WHERE baseid = ?", new String[] { baseId });

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            base = new Base(cursor.getString(0), cursor.getInt(1), cursor.getString(2), (cursor.getInt(3) == 1) ? true : false, cursor.getLong(4));

        }

        cursor.close();
        close();

        return base;
    }

    // Get all Bases
    public ArrayList<Base> getAllBases() {
        ArrayList<Base> baseList = new ArrayList<Base>();

        open();
        Cursor cursor = db.rawQuery("SELECT baseid, base_type, COALESCE(title,''), connected, stamp FROM base", null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Base base = new Base(cursor.getString(0), cursor.getInt(1), cursor.getString(2), (cursor.getInt(3) == 1) ? true : false, cursor.getLong(4));

                // Adding module to list
                baseList.add(base);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        close();

        return baseList;
    }

    /**
     * This updates Base's current connection status if it exists in DB, else it inserts it into DB.
     *
     * @param baseId
     * @param connected
     */
    public void saveBaseConnectedStatus(Context context, String baseId, boolean connected, String title) {
        if (baseId.equals(""))
            return;

        open();
        Cursor cursor = db.rawQuery("SELECT * FROM base WHERE baseid = ?", new String[] { String.valueOf(baseId) });
        if (cursor.moveToFirst()) {
            cursor.close();
            close();

            // UPDATE
            open();
            ContentValues values = new ContentValues();
            values.put("connected", connected ? 1 : 0);
            db.update("base", values, "baseid = ?", new String[] { String.valueOf(baseId) });
            close();

            return;
        }
        else {
            cursor.close();
            close();

            // INSERT
            open();
            ContentValues values = new ContentValues();
            values.put("baseid", baseId);
            values.put("title", title);
            values.put("connected", connected ? 1 : 0);
            values.put("stamp", System.currentTimeMillis());
            db.insert("base", null, values);
            close();

            return;
        }
    }

    public void saveBaseData(String baseId, String data) {
        if (baseId.equals(""))
            return;

        open();
        ContentValues values = new ContentValues();
        values.put("baseid", baseId);
        values.put("data", data);
        values.put("stamp", System.currentTimeMillis());
        db.insert("base_data", null, values);

        close();
    }

    public void updateBaseLastActivity(String baseId) {
        if (baseId.equals(""))
            return;

        open();
        ContentValues values = new ContentValues();
        values.put("stamp", System.currentTimeMillis());
        db.update("base", values, "baseid = ?", new String[] { String.valueOf(baseId) });

        close();
    }

    public void updateBase(Base base) {
        if (base == null || base.getBaseid() == null)
            return;

        open();
        ContentValues values = new ContentValues();
        values.put("title", base.getTitle());
        values.put("base_type", base.getBaseType());
        db.update("base", values, "baseid = ?", new String[] { base.getBaseid() });

        close();
    }

    /**
     * Returns lastest BaseData
     *
     * @return
     */
    public String getLatestBaseData(String baseId) {
        open();

        String ret = "";

        // return information on how many items are still waiting in queue
        Cursor cursorCheck = db.rawQuery("SELECT data FROM base_data WHERE baseid = ? ORDER BY IDbase_data DESC LIMIT 1", new String[] { baseId });
        if (cursorCheck.moveToFirst()) {
            ret = cursorCheck.getString(0);
        }

        close();
        return ret;
    }

    public int getUnseenCount(String baseId) {
        open();

        int ret = 0;

        // return information on how many items are still waiting in queue
        Cursor cursorCheck = db.rawQuery("SELECT COUNT(*) FROM base_data WHERE seen = 0 AND baseid = ?", new String[] { baseId });
        if (cursorCheck.moveToFirst()) {
            ret = cursorCheck.getInt(0);
        }

        close();
        return ret;
    }

    /**
     * Returns Base's connection status.
     *
     * @return true - online, false - offline.
     */
    public boolean getBaseStatus(String baseId) {
        open();

        boolean ret = false;

        // return information on how many items are still waiting in queue
        Cursor cursorCheck = db.rawQuery("SELECT connected FROM base WHERE baseid = ?", new String[] { baseId });
        if (cursorCheck.moveToFirst()) {
            ret = cursorCheck.getInt(0) == 1;
        }

        close();
        return ret;
    }

    /**
     * Flushes entire client2server queue!
     * */
    public void flushTxClient2Server() {
        open();

        db.delete("client2server", "", null);

        close();
    }

    public void deleteBase(String baseId) {
        open();

        db.delete("base", "baseid = ?", new String[] { baseId });
        db.delete("base_data", "baseid = ?", new String[] { baseId });

        close();
    }

    public void unsendAllTxClient2Server() {
        open();

        ContentValues values = new ContentValues();
        values.put("sent", 0);
        db.update("client2server", values, "sent = 1", null);

        close();
    }

    public void unsendAllUnackedTxClient2Server() {
        open();

        ContentValues values = new ContentValues();
        values.put("sent", 0);
        db.update("client2server", values, "acked = 0", null);

        close();
    }

    /**
     * Adding jsonPackage to database which will be sent to Server ASAP.
     *
     * @param jsonPackage
     * @return
     */
    public JSONObject addTxClient2Server(String jsonPackage) {
        JSONObject ret = new JSONObject();

        open();

        int TXclient = 1;
        Cursor cursor = db.rawQuery("SELECT MAX(TXclient) AS nextTXclient FROM client2server", null);
        if (cursor.moveToFirst()) {
            TXclient = cursor.getInt(0) + 1;
        }

        ContentValues values = new ContentValues();
        values.put("json_package", jsonPackage);
        values.put("TXclient", TXclient);
        values.put("sent", 0);
        values.put("acked", 0);
        db.insert("client2server", null, values);

        close();

        return ret;
    }

    /**
     * Returns queueSize and acked in JSON Object.
     * Doesn't return information if item was actually acked in DB... We probably won't need it anyway.
     *
     * @param TXclient
     * @return
     * */
    public JSONObject ackTxClient2Server(int TXclient) {
        open();

        ContentValues values = new ContentValues();
        values.put("acked", 1);
        db.update("client2server", values, "acked = 0 AND TXclient = ?", new String[] { String.valueOf(TXclient) });

        // return information on how many items are still waiting in queue
        Cursor cursorCheck = db.rawQuery("SELECT COUNT(*) AS queue_size FROM client2server WHERE acked = 0", null);
        int queueSize = 0;
        if (cursorCheck.moveToFirst()) {
            queueSize = cursorCheck.getInt(0);
        }
        close();

        JSONObject ret = new JSONObject();
        try {
            ret.put("queueSize", queueSize);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Counts all unacknowledged items from client2server DB.
     *
     * @return
     */
    public int countUnackedTxClient2Server() {
        open();

        int ret = 0;

        // return information on how many items are still waiting in queue
        Cursor cursorCheck = db.rawQuery("SELECT COUNT(*) AS queue_size FROM client2server WHERE acked = 0", null);
        if (cursorCheck.moveToFirst()) {
            ret = cursorCheck.getInt(0);
        }
        else {
            ret = 0;
        }

        close();
        return ret;
    }

    /**
     * Flushes those client2server items until we get to unacked ones.
     */
    public void flushAckedTxClient2Server() {
        open();

        String sql = "DELETE FROM client2server WHERE acked = 1 AND (IDpk < (SELECT MIN(i1.IDpk) FROM client2server i1 WHERE i1.acked = 0) OR (SELECT COALESCE(MIN(i2.IDpk),0) FROM client2server i2 WHERE i2.acked = 0) = 0)";
        db.execSQL(sql);

        close();
    }

    /**
     * Returns TXclient, jsonPackage attributes of client2server table row and also fetched and moreInQueue information in JSON Object.
     * @return
     */
    public JSONObject getNextTxClient2Server() {
        JSONObject ret = new JSONObject();

        open();

        try {
            Cursor cursor = db.rawQuery("SELECT IDpk, TXclient, json_package FROM client2server WHERE acked = 0 AND sent = 0 ORDER BY TXclient ASC LIMIT 1", null);
            if (cursor.moveToFirst()) {
                ret.put("fetched", true);

                ret.put("TXclient", cursor.getInt(1));
                ret.put("jsonPackage", cursor.getString(2));

                // update, set sent = 1
                ContentValues values = new ContentValues();
                values.put("sent", 1);
                db.update("client2server", values, "IDpk = ?", new String[] { String.valueOf(cursor.getInt(0)) });

                // check to see if there is more in queue waiting to be sent
                Cursor cursorCheck = db.rawQuery("SELECT IDpk FROM client2server WHERE acked = 0 AND sent = 0 LIMIT 1", null);
                if (cursorCheck.moveToFirst()) {
                    ret.put("moreInQueue", cursorCheck.getCount() > 0);
                }
                else {
                    ret.put("moreInQueue", false);
                }
            }
            else {
                ret.put("fetched", false);
                ret.put("moreInQueue", false);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            close();
        }

        return ret;
    }

    /**
     * Get pubvar with default value on no-value from DB.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getPubVar(String key, String defaultValue) {
        String r = getPubVar(key);
        if (r == null || r.equals(""))
            return defaultValue;
        else
            return r;
    }

    /**
     * Get pubvar without default value. On no-value returns empty string.
     *
     * @param key
     * @return
     */
    public String getPubVar(String key) {
        if (key.equals("")) {
            return "";
        }

        open();
        Cursor cursor = db.rawQuery("SELECT val FROM pubvar WHERE key = ?", new String[] { key });

        if (cursor.moveToFirst()) {
            String rez = cursor.getString(0);
            cursor.close();
            close();
            return rez;
        }
        else {
            cursor.close();
            close();
            return "";
        }
    }

    /**
     * Saves pubvar value.
     *
     * @param key
     * @param val
     * @return
     */
    public boolean savePubVar(String key, String val) {
        if (key.equals("")) {
            return false;
        }

        open();
        Cursor cursor = db.rawQuery("SELECT val FROM pubvar WHERE key = ?", new String[] { key });
        if (cursor.moveToFirst()) {
            cursor.close();
            close();

            // UPDATE
            open();
            ContentValues values = new ContentValues();
            values.put("val", val);
            db.update("pubvar", values, "key = ?", new String[] { key });
            close();

            return true;
        }
        else {
            cursor.close();
            close();

            // INSERT
            open();
            ContentValues values = new ContentValues();
            values.put("key", key);
            values.put("val", val);
            db.insert("pubvar", null, values);
            close();

            return true;
        }
    }

    public void markBaseDataSeen(String baseId) {
        open();

        ContentValues values = new ContentValues();
        values.put("seen", 1);
        db.update("base_data", values, "seen = 0 AND baseid = ?", new String[] { baseId });

        close();
    }

}
