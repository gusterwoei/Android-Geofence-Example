package com.guster.androidgeofence.db.repository;

import android.content.Context;
import android.database.Cursor;

import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.domain.MyGeofenceEvent;
import com.guster.skydb.Repository;

import java.util.List;

/**
 * Created by Gusterwoei on 9/17/15.
 *
 */
public class MyGeofenceEventRepository extends Repository<MyGeofenceEvent> {
    private static MyGeofenceEventRepository myGeofenceEventRepository;

    public static MyGeofenceEventRepository getInstance(Context context) {
        if(myGeofenceEventRepository == null)
            myGeofenceEventRepository = new MyGeofenceEventRepository(context);
        return myGeofenceEventRepository;
    }

    public MyGeofenceEventRepository(Context context) {
        super(context, MyGeofenceEvent.class);
    }

    @Override
    public List<MyGeofenceEvent> findAll() {
        List<MyGeofenceEvent> list = super.findAllOrderBy("date", true);

        MyGeofenceRepository repo = MyGeofenceRepository.getInstance(context);
        for(MyGeofenceEvent event : list) {
            MyGeofence geo = repo.findById(event.getId());
            event.setPlaceName(geo.getTitle());
        }

        return list;
    }
}
