package com.guster.androidgeofence.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.db.repository.MyGeofenceEventRepository;
import com.guster.androidgeofence.db.repository.MyGeofenceRepository;
import com.guster.androidgeofence.service.GeofenceService;

/**
 * Created by Gusterwoei on 9/23/15.
 *
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public MyGeofenceRepository getMyGeofenceRepository() {
        return MyGeofenceRepository.getInstance(getApplicationContext());
    }

    public MyGeofenceEventRepository getMyGeofenceEventRepository() {
        return MyGeofenceEventRepository.getInstance(getApplication());
    }

    public Util getUtil() {
        return Util.getInstance(getApplicationContext());
    }

    public GeofenceService getGeofenceService() {
        return GeofenceService.getInstance(getApplicationContext());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
