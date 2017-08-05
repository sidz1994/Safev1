package com.example.bharath.safev1;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class Stopservices_service extends IntentService {

    public Stopservices_service() {
        super("Stopservices_service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if(isMyServiceRunning(Senddata_service.class))
            {
                    stopService(new Intent(this, Senddata_service.class));
                    stopSelf();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}



