package com.guster.androidgeofence.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.guster.androidgeofence.R;
import com.guster.androidgeofence.Util;
import com.guster.androidgeofence.activity.GeofencingActivity;
import com.guster.androidgeofence.db.domain.MyGeofence;
import com.guster.androidgeofence.db.domain.MyGeofenceEvent;
import com.guster.androidgeofence.db.repository.MyGeofenceEventRepository;
import com.guster.androidgeofence.db.repository.MyGeofenceRepository;

import java.util.List;

/**
 * Created by Gusterwoei on 9/17/15.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.log("receiver received: " + intent.getAction());
        //Toast.makeText(context, "receiver received: " + intent.getAction(), Toast.LENGTH_SHORT).show();

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event.hasError()) {
            String error = "Location Intent Service error: " + event.getErrorCode() + "";
            Util.error(error);
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            sendNotification(context, error, 99);
            return;
        }

        int transitionType = event.getGeofenceTransition();
        List<Geofence> geofences = event.getTriggeringGeofences();

        Util.log("transition type: " + transitionType);
        //Toast.makeText(context, "transition type: " + transitionType, Toast.LENGTH_SHORT).show();

        switch(transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER: {
                for(Geofence geofence : geofences) {
                    handleGeoEvent(context, geofence, "ENTER");
                }
                break;
            }
            case Geofence.GEOFENCE_TRANSITION_EXIT: {
                for(Geofence geofence : geofences) {
                    handleGeoEvent(context, geofence, "EXIT");
                }
                break;
            }
            case Geofence.GEOFENCE_TRANSITION_DWELL: {
                for(Geofence geofence : geofences) {
                    handleGeoEvent(context, geofence, "DWELL");
                }
                break;
            }
        }
    }

    private void handleGeoEvent(Context context, Geofence geofence, String event) {
        boolean isEnter = event.equalsIgnoreCase("ENTER");

        MyGeofenceRepository myGeofenceRepository = MyGeofenceRepository.getInstance(context);
        MyGeofenceEventRepository myGeofenceEventRepository = MyGeofenceEventRepository.getInstance(context);
        List<MyGeofence> myGeofences = myGeofenceRepository.findBy("id", geofence.getRequestId());
        MyGeofence myGeofence = myGeofences.isEmpty()? null : myGeofences.get(0);

        //String msg = event + " Geofence: " + geofence.getRequestId();
        //Util.log(msg);

        if(myGeofence == null) return;

        // save event to database
        MyGeofenceEvent myGeofenceEvent = new MyGeofenceEvent(myGeofence);
        myGeofenceEvent.setEvent(event.toUpperCase());
        myGeofenceEventRepository.save(myGeofenceEvent);

        // send broadcast
        Util.log("Sending broadcast...");
        context.sendBroadcast(new Intent("com.guster.experiment.ACTION_GEO_CHANGED"));

        if(event.equalsIgnoreCase("ENTER"))
            sendNotification(context, "You have ENTER into " + myGeofence.getTitle(), 1);
        else if(event.equalsIgnoreCase("EXIT"))
            sendNotification(context, "You have EXIT from " + myGeofence.getTitle(), 2);
        else
            sendNotification(context, "You are DWELLING in " + myGeofence.getTitle(), 3);
    }

    private void sendNotification(Context context, String message, int requestID) {
        //int requestID = new Random().nextInt(100);
        Intent i = new Intent(context, GeofencingActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestID, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(R.drawable.app_icon_ori)
                .setTicker(message)
                .setContentTitle("Geofence")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .build();
        notificationManager.notify(requestID, notification);
    }
}
