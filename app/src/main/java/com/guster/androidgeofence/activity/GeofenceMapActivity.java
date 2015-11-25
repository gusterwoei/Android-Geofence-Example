package com.guster.androidgeofence.activity;

import android.app.Dialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.guster.androidgeofence.R;
import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.repository.MyGeofenceRepository;
import com.guster.androidgeofence.service.GeofenceService;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gusterwoei on 9/22/15.
 *
 */
public class GeofenceMapActivity extends BaseActivity implements View.OnClickListener, LocationListener {
    public static final String EXTRA_MYGEO_ID = "extra mygeo id";
    private MapView mapView;
    private Button btnShowMe, btnAddGeofence;

    // data
    private GoogleMap map;
    private Marker meMarker;
    private MyGeofence selectedMyGeo;
    private HashMap<String, MyGeofence> markers = new HashMap<String, MyGeofence>();
    private ScheduledFuture timerTask;
    private LocationManager locationManager;
    private boolean trackCurrentLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_map);
        mapView = (MapView) findViewById(R.id.mapview);
        btnShowMe = (Button) findViewById(R.id.btn_show_me);
        btnAddGeofence = (Button) findViewById(R.id.btn_add_geofence);

        if(getIntent() != null) {
            String myGeoId = getIntent().getStringExtra(EXTRA_MYGEO_ID);
            selectedMyGeo = getMyGeofenceRepository().findById(myGeoId);
            if(myGeoId != null)
                trackCurrentLocation = false;
        }

        // initialize timer
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ScheduledThreadPoolExecutor timerService = new ScheduledThreadPoolExecutor(1);
        timerTask = timerService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //Util.log("timer tick");
                requestLocationUpdate();
            }
        }, 0, 3, TimeUnit.SECONDS);

        btnShowMe.setOnClickListener(this);
        btnAddGeofence.setOnClickListener(this);
        mapView.onCreate(savedInstanceState);

        setFormData();
    }

    private void setFormData() {
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                setupGoogleMap(googleMap);
            }
        });
    }

    private void setupGoogleMap(GoogleMap googleMap) {
        map = googleMap;
        map.setBuildingsEnabled(true);
        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                trackCurrentLocation = true;
                return false;
            }
        });

        MyGeofenceRepository repo = MyGeofenceRepository.getInstance(getApplicationContext());
        List<MyGeofence> list = repo.findAll();

        // setup all geoFence markers
        for (MyGeofence mygeo : list) {
            //LatLng latLng = new LatLng(mygeo.getLatitude(), mygeo.getLongitude());

            // draw geoFence circle
            addCircle(mygeo.getLatitude(), mygeo.getLongitude(), mygeo.getRadius());

            // draw a marker and store into the list
            addMarker(mygeo);
        }

        // on marker click listener
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MyGeofence mygeo = markers.get(marker.getId());
                if(mygeo != null) {
                    Util.log("marker " + marker.getId() + " clicked, geoId: " + mygeo.getId());
                }
                return false;
            }
        });

        // if there is no selected geoFence, go to current location instead
        if (selectedMyGeo != null) {
            zoomTo(selectedMyGeo.getLatitude(), selectedMyGeo.getLongitude());
        } else {
            zoomInToCurrentLocation();
        }
    }

    private void requestLocationUpdate() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                locationManager.requestLocationUpdates(GeofencingActivity.LOCATION_PROVIDER, 0, 0, GeofenceMapActivity.this);
            }
        });
    }

    private void zoomInToCurrentLocation() {
        Location location = getUtil().getCurrentDeviceLocation(GeofencingActivity.LOCATION_PROVIDER);
        if (map != null && location != null) {
            map.animateCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.5f));

            /*if(meMarker != null)
                meMarker.remove();
            meMarker = map.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_boy))
                    .position(new LatLng(location.getLatitude(), location.getLongitude())));*/
        }
    }

    private void zoomTo(double lat, double lng) {
        if(map == null) {
            Util.error("map is not ready to zoom yet");
            return;
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17.5f));
    }

    private void addMarker(MyGeofence mygeo) {
        LatLng latLng = new LatLng(mygeo.getLatitude(), mygeo.getLongitude());
        Marker marker = map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_boy_green))
                .title(mygeo.getTitle())
                .position(latLng));
        marker.showInfoWindow();
        markers.put(marker.getId(), mygeo);
    }

    private void addCircle(double lat, double lng, float radius) {
        map.addCircle(new CircleOptions()
                .center(new LatLng(lat, lng))
                .radius(radius)
                .strokeWidth(0f)
                .fillColor(getResources().getColor(R.color.transparent_black)));
    }

    @Override
    public void onClick(View view) {
        if(view == btnShowMe) {
            zoomInToCurrentLocation();

        } else if(view == btnAddGeofence) {
            getGeofenceService().showAddNewGeofenceDialog(this, new GeofenceService.OnAddGeofenceListener() {
                @Override
                public void onAddGeofence(Dialog dialog, MyGeofence myGeofence) {
                    addMarker(myGeofence);
                    addCircle(myGeofence.getLatitude(), myGeofence.getLongitude(), myGeofence.getRadius());

                    setResult(RESULT_OK);
                }
            });
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if(trackCurrentLocation)
            zoomInToCurrentLocation();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        if(timerTask != null) {
            timerTask.cancel(true);
        }
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
