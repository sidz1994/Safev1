package com.example.bharath.safev1;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;

public class Settings extends AppCompatActivity implements View.OnClickListener {
    Button not_on,not_off;
    String uid="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent=getIntent();
        uid=intent.getExtras().getString("uid");
        not_on = (Button) findViewById(R.id.button);
        not_on.setOnClickListener(this);
        not_on.setEnabled(true);

        not_off = (Button) findViewById(R.id.button2);
        not_off.setOnClickListener(this);
        not_off.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                notifieron();
                break;
            case R.id.button2:
                notifyoff();
                break;
            default:
                break;

        }
    }

    public void notifieron()
    {
        Intent alert_intent = new Intent(this, Senddata_service.class);
        alert_intent.putExtra("uid",uid);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent p_alert_Intent = PendingIntent.getService(this, 0, alert_intent, 0 );//PendingIntent.FLAG_UPDATE_CURRENT
        Intent stop_intent = new Intent(this, Stopservices_service.class);
        PendingIntent p_stop_Intent = PendingIntent.getService(this, 0, stop_intent, 0 );//PendingIntent.FLAG_UPDATE_CURRENT

        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("title");
        notificationBuilder.setContentText("message");
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setVisibility(1);//Notification.VISIBILITY_PUBLIC
        notificationBuilder.setPriority(2);//Notification.PRIORITY_MAX
        notificationBuilder.setContentIntent(p_alert_Intent);
        notificationBuilder.setOngoing(true);
        NotificationCompat.Action alert_action = new NotificationCompat.Action.Builder(R.drawable.ic_notifications_black_24dp, "Alert", p_alert_Intent).build();
        notificationBuilder.addAction(alert_action);
        notificationBuilder.setContentIntent(p_stop_Intent);
        NotificationCompat.Action track_action = new NotificationCompat.Action.Builder(R.drawable.ic_notifications_black_24dp, "Track", p_stop_Intent).build();
        notificationBuilder.addAction(track_action);
        NotificationCompat.Action stop_action = new NotificationCompat.Action.Builder(R.drawable.ic_notifications_black_24dp, "Stop", p_stop_Intent).build();
        notificationBuilder.addAction(stop_action);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());
    }

    public void notifyoff()
    {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(0);
    }
}


