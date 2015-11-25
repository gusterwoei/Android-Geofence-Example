package com.guster.androidgeofence;

import android.app.Application;

import com.guster.androidgeofence.db.DbHelper;
import com.guster.androidgeofence.db.repository.MyGeofenceEventRepository;

/**
 * Created by Gusterwoei on 12/22/14.
 *
 */
public class MyApplication extends Application {
    public static final String DB_NAME = "geofence.db";
    public static final int DB_VERSION = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
    }

    private void init() {
        // initialize database
        DbHelper.getInstance(this).getWritableDatabase();

        // initialize preference settings
        Util util = Util.getInstance(this);
        float radius = (float) util.getPreference(PrefKey.PREF_RADIUS, -1f);
        if(radius < 0) {
            util.savePreference(PrefKey.PREF_RADIUS, 50f);
        }
    }
}
