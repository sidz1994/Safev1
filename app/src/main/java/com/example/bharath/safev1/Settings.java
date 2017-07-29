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
        Intent intent = new Intent(this, Senddata_service.class);
        intent.putExtra("uid",uid);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getService(this, 0, intent, 0 );//PendingIntent.FLAG_UPDATE_CURRENT

        NotificationCompat.Builder notificationBuilder =new NotificationCompat.Builder(this);
        notificationBuilder.setContentTitle("title");
        notificationBuilder.setContentText("message");
        notificationBuilder.setAutoCancel(false);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setVisibility(1);//Notification.VISIBILITY_PUBLIC
        notificationBuilder.setPriority(2);//Notification.PRIORITY_MAX
        notificationBuilder.setContentIntent(pIntent);
        notificationBuilder.addAction(R.drawable.ic_notifications_black_24dp, "Alert", pIntent);
        notificationBuilder.addAction(R.drawable.ic_notifications_black_24dp, "Track", pIntent);
        notificationBuilder.addAction(R.drawable.ic_notifications_black_24dp, "Stop", pIntent);
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuilder.build());


    }

    public void notifyoff()
    {
        NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(0);
    }
}


