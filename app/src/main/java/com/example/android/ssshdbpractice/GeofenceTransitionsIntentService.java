package com.example.android.ssshdbpractice;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.places.GeoDataClient;

import java.util.ArrayList;
import java.util.List;


public class GeofenceTransitionsIntentService extends IntentService {

    private String title;
    private String description;
    private String mode;

    private static final String TAG = "GeofenceTransitionsIS";

    String geofenceTransitionString;


    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {

            return;
        }

        title = intent.getStringExtra("title");
        description = intent.getStringExtra("description");
        mode = intent.getStringExtra("mode");

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
                    triggeringGeofences);


            sendNotification(geofenceTransitionString + ": " + title);

            if (mode.equals("general")) {
                setRingerMode(getApplicationContext(), AudioManager.RINGER_MODE_NORMAL);
            } else if (mode.equals("silent")) {
                setRingerMode(getApplicationContext(), AudioManager.RINGER_MODE_SILENT);
            } else if (mode.equals("vibrate")) {
                setRingerMode(getApplicationContext(), AudioManager.RINGER_MODE_VIBRATE);
            }

            Log.i(TAG, geofenceTransitionDetails);

        } else {
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setRingerMode(Context context, int mode) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT < 24 || (Build.VERSION.SDK_INT >= 24 && nm.isNotificationPolicyAccessGranted())) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
    }

    private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        geofenceTransitionString = getTransitionString(geofenceTransition);

        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());


        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);


        return triggeringGeofencesIdsString;
    }

    private void sendNotification(String notificationDetails) {
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(
                R.mipmap.ic_launcher)
                .setContentTitle(notificationDetails)
                .setContentText(description)
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        if (mode.equals("general")) {
            builder.setSmallIcon(
                    R.drawable.general_mode);
        } else if (mode.equals("silent")) {
            builder.setSmallIcon(
                    R.drawable.silent_mode);
        } else if (mode.equals("vibrate")) {
            builder.setSmallIcon(
                    R.drawable.vibrate_mode);
        }

        builder.setAutoCancel(true);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }


    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
