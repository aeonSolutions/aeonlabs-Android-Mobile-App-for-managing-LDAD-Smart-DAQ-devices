package aeonlabs.iot.data.acquisition.home.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import aeonlabs.iot.data.acquisition.home.model.SettingsDB;

public class SettingsDatabaseHelper extends SQLiteOpenHelper {
    private static final String LOG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "settings";
    private static final String TABLE_SETTINGS = "settings";


    private static final String CREATE_TABLE_INSTRUCTOR = "create table if not exists "
            + TABLE_SETTINGS
            + " (id integer primary key autoincrement, cloud_api_address varchar(255));";

    public SettingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(CREATE_TABLE_INSTRUCTOR);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        onCreate(db);
    }

    public void SettingsSQLquery(String sql){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(sql);
        db.close();
    }

    public void addSettings(SettingsDB settingsDB) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("cloud_api_address", settingsDB.getCloudAPIaddress());

        db.insert(TABLE_SETTINGS, null, values);
        db.close();

    }


    public ArrayList<Object> getAllSettings() {
        ArrayList<Object> settingsArrayList = new ArrayList<Object>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS+" ORDER BY id DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SettingsDB settingsDB = new SettingsDB();
                settingsDB.setSettingsId(cursor.getString(0));
                settingsDB.setCloudAPIaddress(cursor.getString(1));

                // Adding contact to list
                settingsArrayList.add(settingsDB);
            } while (cursor.moveToNext());
        }

        // return contact list
        return settingsArrayList;
    }
}

