package com.guster.androidgeofence.service;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.guster.androidgeofence.PrefKey;
import com.guster.androidgeofence.R;
import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.repository.MyGeofenceEventRepository;
import com.guster.androidgeofence.db.repository.MyGeofenceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Gusterwoei on 9/23/15.
 */
public class GeofenceService {
    public static String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;
    private static GeofenceService geofenceService;
    private Context context;
    private LocationManager locationManager;
    private List<Geofence> geofences = new ArrayList<>();

    public interface OnAddGeofenceListener {
        void onAddGeofence(Dialog dialog, MyGeofence myGeofence);
    }

    public static GeofenceService getInstance(Context context) {
        if(geofenceService == null)
            geofenceService = new GeofenceService(context);
        return geofenceService;
    }

    private GeofenceService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void showAddNewGeofenceDialog(Activity activity, final OnAddGeofenceListener listener) {
        final Dialog dialog = getUtil().createDialog(activity, R.layout.dialog_edit_text);
        dialog.setTitle("Add New Geo");

        Button btnDone = (Button) dialog.findViewById(R.id.btn_done);
        final EditText edtName = (EditText) dialog.findViewById(R.id.edit_text);
        edtName.setHint("Give a name");

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtName.getText().toString().trim().isEmpty()) {
                    edtName.setError("Give a name for this place");
                } else {
                    String name = edtName.getText().toString().trim();
                    MyGeofence myGeofence = addNewGeofence(name);
                    registerGeofences();

                    // callback too caller
                    if (listener != null)
                        listener.onAddGeofence(dialog, myGeofence);

                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    public MyGeofence addNewGeofence(String placeTitle) {
        Location location = locationManager.getLastKnownLocation(LOCATION_PROVIDER);
        if(location == null) {
            Toast.makeText(context, "No location found", Toast.LENGTH_LONG).show();
            return null;
        }

        // save to db
        String id = UUID.randomUUID().toString();
        float radius = (float) getUtil().getPreference(PrefKey.PREF_RADIUS, 50f);
        MyGeofence myGeofence = new MyGeofence(id, location.getLatitude(), location.getLongitude(), radius);
        myGeofence.setTitle(placeTitle);
        getMyGeofenceRepository().save(myGeofence);

        Toast.makeText(context, "Geofence set", Toast.LENGTH_SHORT).show();

        return myGeofence;
    }

    public void registerGeofences() {
        geofences.clear();
        List<MyGeofence> myGeofences = getMyGeofenceRepository().findAll();
        for(MyGeofence mygeo : myGeofences) {
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(mygeo.getId())
                    .setCircularRegion(mygeo.getLatitude(), mygeo.getLongitude(), mygeo.getRadius())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(3000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                    .build();
            geofences.add(geofence);
        }
    }

    public List<Geofence> getGeofences() {
        return geofences;
    }

    private Util getUtil() {
        return Util.getInstance(context);
    }

    private MyGeofenceRepository getMyGeofenceRepository() {
        return MyGeofenceRepository.getInstance(context);
    }

    private MyGeofenceEventRepository getMyGeofenceEventRepository() {
        return MyGeofenceEventRepository.getInstance(context);
    }
}
