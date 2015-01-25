package ba.ctrl.ctrltest1.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 2;

    // Database Name
    public static final String DATABASE_NAME = "Ctrl.db";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private void createTableClient2Server(SQLiteDatabase db) {
        String sql = "CREATE TABLE client2server (" +
                "IDpk INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
        		"TXclient INTEGER," +
        		"json_package TEXT," +
        		"sent INTEGER," +
        		"acked INTEGER" +
        		");";

        db.execSQL(sql);
    }

    private void createTableBase(SQLiteDatabase db) {
        String sql = "CREATE TABLE base (" +
                "baseid TEXT PRIMARY KEY NOT NULL," +
                "base_type INTEGER DEFAULT 0," +
                "title TEXT," +
                "connected INTEGER," +
                "stamp INTEGER" +
                ");";

        db.execSQL(sql);
    }

    private void createTableBaseData(SQLiteDatabase db) {
        String sql = "CREATE TABLE base_data (" +
        		"IDbase_data INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
        		"baseid TEXT NOT NULL," +
        		"stamp INTEGER," +
        		"data TEXT," +
        		"seen INTEGER DEFAULT 0" +
        		");";

        db.execSQL(sql);
    }

    // KEY-VAL Settings for Global usage
    private void createTablePubVar(SQLiteDatabase db) {
        String sql = "CREATE TABLE pubvar ("
                + "IDpubvar INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
                + "key TEXT,"
                + "val TEXT);";
        db.execSQL(sql);
    }

    private void createDB(SQLiteDatabase db)
    {
        createTableBase(db);
        createTableBaseData(db);
        createTableClient2Server(db);
        createTablePubVar(db);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        createDB(db);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS base");
            db.execSQL("DROP TABLE IF EXISTS base_data");
            db.execSQL("DROP TABLE IF EXISTS client2server");
            db.execSQL("DROP TABLE IF EXISTS pubvar");

            createDB(db);
        }
    }

}
