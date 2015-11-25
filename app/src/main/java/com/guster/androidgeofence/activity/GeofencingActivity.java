package com.guster.androidgeofence.activity;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.guster.androidgeofence.MyApplication;
import com.guster.androidgeofence.R;
import com.guster.androidgeofence.StandardListAdapter;
import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.domain.MyGeofenceEvent;
import com.guster.androidgeofence.service.GeofenceService;
import java.util.List;


/**
 * Created by Gusterwoei on 9/17/15.
 *
 */
public class GeofencingActivity extends BaseActivity
        implements View.OnClickListener, LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final int REQ_CODE_GO_MAP = 1;
    public static String LOCATION_PROVIDER = GeofenceService.LOCATION_PROVIDER;
    private Button btnAddNewGeofence, btnReset, btnMyGeo, btnMap;
    private ListView listData;
    private View lytPlaceholder, lytContent;
    private TextView btnClear;
    private SwipeRefreshLayout lytSwipeRefresh;

    private LocationManager locationManager;
    private GoogleApiClient googleApiClient;
    private PendingIntent geofenceRequestIntent;
    private StandardListAdapter<MyGeofenceEvent> myGeoEventAdapter;
    private StandardListAdapter<MyGeofence> myGeoAdapter;
    private BroadcastReceiver receiver;
    private boolean isGeofenceRequestStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        setContentView(R.layout.activity_geofencing);
        btnAddNewGeofence = (Button) findViewById(R.id.btn_start_stop);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnMyGeo = (Button) findViewById(R.id.btn_mygeo);
        btnMap = (Button) findViewById(R.id.btn_map);
        listData = (ListView) findViewById(R.id.list_data);
        lytPlaceholder = findViewById(R.id.lyt_placeholder);
        lytContent = findViewById(R.id.lyt_content);
        btnClear = (TextView) findViewById(R.id.btn_clear);
        lytSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.lyt_swipe_refresh);

        btnAddNewGeofence.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnMyGeo.setOnClickListener(this);
        btnMap.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        lytSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadFormData();
            }
        });

        setupGoogleApi();
        getGeofenceService().registerGeofences();

        //geofences = getGeofenceService().getGeofences();

        loadFormData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_debug:
                getUtil().saveDbToSdCard(MyApplication.DB_NAME);
                Snackbar.make(findViewById(R.id.root_view), "Extracting database", Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "Extracting database", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //LOCATION_PROVIDER = locationManager.getBestProvider(criteria, true);
        //Util.log("best location provider goes to... " + LOCATION_PROVIDER);

        requestLocationUpdate();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Util.log("receiver received: " + intent.getAction());
                loadFormData();
            }
        };
        registerReceiver(receiver, new IntentFilter("com.guster.experiment.ACTION_GEO_CHANGED"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationServices.GeofencingApi.removeGeofences(googleApiClient, getGeofenceRequestIntent());
    }

    private void requestLocationUpdate() {
        locationManager.requestLocationUpdates(LOCATION_PROVIDER, 0, 0, this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnAddNewGeofence) {
            getGeofenceService().showAddNewGeofenceDialog(this, new GeofenceService.OnAddGeofenceListener() {
                @Override
                public void onAddGeofence(Dialog dialog, MyGeofence myGeofence) {
                    // refresh the data
                    loadFormData();

                    // start geofence request if it isn't started yet
                    if(!isGeofenceRequestStarted)
                        startGeofenceRequest();
                }
            });

        } else if(view == btnMyGeo) {
            showMyGeofences();

        } else if(view == btnReset) {
            resetMyGeofences();

        } else if(view == btnMap) {
            startActivityForResult(new Intent(this, GeofenceMapActivity.class), REQ_CODE_GO_MAP);

        } else if(view == btnClear) {
            getUtil().showConfirmationDialog(this, "CLEAR ACTIVITY", "Clear all recent activities?",
                    "CLEAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getMyGeofenceEventRepository().deleteAll();
                            loadFormData();
                        }
                    }, null, null);
        }
    }

    private void setupGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }



    private GeofencingRequest getGeofenceRequest() {
        // get all geofences
        List<Geofence> geofences = getGeofenceService().getGeofences();

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL);
        builder.addGeofences(geofences);

        if(geofences.isEmpty())
            return null;
        return builder.build();
    }

    private PendingIntent getGeofenceRequestIntent() {
        if(geofenceRequestIntent != null)
            return geofenceRequestIntent;

        //Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        //return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent("com.guster.experiment.ACTION_GEO_CHANGED");
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void showMyGeofences() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_listview);
        dialog.setTitle("My Geofences");

        List<MyGeofence> data = getMyGeofenceRepository().findAll();
        ListView listView = (ListView) dialog.findViewById(R.id.list_data);

        myGeoAdapter = new StandardListAdapter<>(getApplicationContext(), R.layout.listitem_geofence, data, new StandardListAdapter.ListAdapterListener() {
            @Override
            public View getView(int i, Object item, View view, ViewGroup parent) {
                final MyGeofence myGeofence = (MyGeofence) item;
                TextView txtId = (TextView) view.findViewById(R.id.txt_geo_id);
                TextView txtLat = (TextView) view.findViewById(R.id.txt_lat);
                TextView txtLng = (TextView) view.findViewById(R.id.txt_lng);
                TextView txtRadius = (TextView) view.findViewById(R.id.txt_radius);
                View btnRemove = view.findViewById(R.id.btn_remove);

                txtId.setText(myGeofence.getTitle());
                txtLat.setText("LAT: " + myGeofence.getLatitude() + "");
                txtLng.setText("LNG: " + myGeofence.getLongitude() + "");
                txtRadius.setText("Radius: " + myGeofence.getRadius() + "m");

                // remove button clicked
                btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showRemoveDialog(myGeofence);
                    }
                });

                return view;
            }

            private void showRemoveDialog(final MyGeofence myGeofence) {
                getUtil().showConfirmationDialog(GeofencingActivity.this, "Delete", "Confirm to delete '" + myGeofence.getTitle() + "'?",
                        "DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // first we delete all related geofence events
                                getMyGeofenceEventRepository().deleteBy("id", myGeofence.getId());

                                // delete the geofence
                                getMyGeofenceRepository().deleteBy("id", myGeofence.getId());

                                // refresh myGeo list
                                List<MyGeofence> data = getMyGeofenceRepository().findAll();
                                myGeoAdapter.setData(data);

                                // refresh myGeo event list
                                loadFormData();
                            }
                        }, null, null);
            }

            @Override
            public String getFilterCriteria(Object item) {
                return null;
            }
        });
        listView.setAdapter(myGeoAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MyGeofence mygeo = myGeoAdapter.getItem(i);

                Intent intent = new Intent(getApplicationContext(), GeofenceMapActivity.class);
                intent.putExtra(GeofenceMapActivity.EXTRA_MYGEO_ID, mygeo.getId());
                startActivity(intent);
            }
        });
        dialog.show();
    }

    private void resetMyGeofences() {
        getUtil().showConfirmationDialog(this, "Reset", "Confirm to reset all Geofences?",
                "RESET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getMyGeofenceRepository().deleteAll();
                        getMyGeofenceEventRepository().deleteAll();
                        loadFormData();
                    }
                }, "CANCEL", null);
        /*final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("Reset");
        dialog.setMessage("Confirm to reset all Geofences?");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getMyGeofenceRepository().deleteAll();
                getMyGeofenceEventRepository().deleteAll();
                loadFormData();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        dialog.show();*/
    }

    private void loadFormData() {
        Util.runAsync(new Util.AsyncCallback<List<MyGeofenceEvent>>() {
            @Override
            public List<MyGeofenceEvent> doInBackground() {
                return getMyGeofenceEventRepository().findAll();
            }

            @Override
            public void onPostExecute(List<MyGeofenceEvent> data) {
                lytSwipeRefresh.setRefreshing(false);
                setFormData(data);
            }
        });
    }

    private void setFormData(List<MyGeofenceEvent> data) {
        if(data.isEmpty()) {
            listData.setVisibility(View.GONE);
            lytPlaceholder.setVisibility(View.VISIBLE);
        } else {
            listData.setVisibility(View.VISIBLE);
            lytPlaceholder.setVisibility(View.GONE);
        }

        if(myGeoEventAdapter != null) {
            myGeoEventAdapter.setData(data);
            return;
        }

        myGeoEventAdapter = new StandardListAdapter<>(getApplicationContext(), R.layout.listitem_geofence, data, new StandardListAdapter.ListAdapterListener() {
            @Override
            public View getView(int i, Object item, View view, ViewGroup parent) {
                MyGeofenceEvent event = (MyGeofenceEvent) item;
                TextView txtId = (TextView) view.findViewById(R.id.txt_geo_id);
                TextView txtLat = (TextView) view.findViewById(R.id.txt_lat);
                TextView txtLng = (TextView) view.findViewById(R.id.txt_lng);
                TextView txtRadius = (TextView) view.findViewById(R.id.txt_radius);
                TextView txtEvent = (TextView) view.findViewById(R.id.txt_event);
                ImageView btnRemove = (ImageView) view.findViewById(R.id.btn_remove);

                txtId.setText(event.getPlaceName());
                txtLat.setText("LAT: " + event.getLatitude() + "");
                txtLng.setText("LNG: " + event.getLongitude() + "");
                txtRadius.setText("Radius: " + event.getRadius() + "m");
                txtEvent.setVisibility(View.VISIBLE);
                txtEvent.setText(event.getEvent());
                btnRemove.setVisibility(View.GONE);

                return view;
            }

            @Override
            public String getFilterCriteria(Object item) {
                return null;
            }
        });
        listData.setAdapter(myGeoEventAdapter);
    }

    private void startGeofenceRequest() {
        Util.log("(starting geofence request...)");
        GeofencingRequest request = getGeofenceRequest();
        if (request == null) {
            Util.error("Geofence request is null");
            return;
        }

        LocationServices.GeofencingApi
                .addGeofences(googleApiClient, request, getGeofenceRequestIntent())
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Util.log("Location Result Callback: " + status);

                        // success
                        if (status.getStatusCode() == 0) {
                            isGeofenceRequestStarted = true;
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK) return;

        switch (requestCode) {
            case REQ_CODE_GO_MAP: {
                loadFormData();
                break;
            }
        }
    }

    /**
     * Location Listener
     */
    @Override
    public void onLocationChanged(Location location) {
        String msg = "Location changed to -> " + location.getLatitude() + ", " + location.getLongitude();
        Util.log(msg);
        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Util.log("Location status change: " + s + ", " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Util.log("Location provider enabled: " + s);
        if(!isGeofenceRequestStarted) {
            startGeofenceRequest();
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        Util.log("Location provider disabled: " + s);
        if(isGeofenceRequestStarted) {
            isGeofenceRequestStarted = false;
        }
    }

    /**
     * Google API Client connection listener
     */
    @Override
    public void onConnected(Bundle bundle) {
        Util.log("Google API connected.");
        Toast.makeText(getApplicationContext(), "Google API connected.", Toast.LENGTH_LONG).show();

        startGeofenceRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Util.error("Google API connection suspended");
    }

    /**
     * Google API Client connection failed listener
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Util.error("Google API connection failed.");
    }
}
