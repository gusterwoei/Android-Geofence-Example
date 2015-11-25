package com.guster.androidgeofence.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.guster.androidgeofence.MyApplication;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.domain.MyGeofenceEvent;
import com.guster.skydb.SkyDatabase;

/**
 * Created by Gusterwoei on 12/22/14.
 *
 */
public class DbHelper extends SkyDatabase {
    private static DbHelper dbHelper;

    public static DbHelper getInstance(Context context) {
        if(dbHelper == null) {
            dbHelper = new DbHelper(context.getApplicationContext());
        }
        return dbHelper;
    }

    private DbHelper(Context context) {
        super(context, MyApplication.DB_NAME, MyApplication.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, DatabaseHelper databaseHelper) {
        databaseHelper.createTable(MyGeofence.class);
        databaseHelper.createTable(MyGeofenceEvent.class);
    }

    @Override
    public void onMigrate(SQLiteDatabase sqLiteDatabase, int i, DatabaseHelper databaseHelper) {

    }
}
