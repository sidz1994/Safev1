package com.example.bharath.safev1;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by BHARATH on 19-May-17.
 */

public class FcmInstanceIdService extends FirebaseInstanceIdService {


    final static String MY_ACTION = "MY_ACTION";

    @Override
    public void onTokenRefresh(){
        String recent_token= FirebaseInstanceId.getInstance().getToken();



        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(getString(R.string.FCM_TOKEN),recent_token);
        editor.commit();


        //from her
        /*Intent intent = new Intent();
        intent.setAction(MY_ACTION);

        intent.putExtra("fcm_token", recent_token);
        this.sendBroadcast(intent);*/
    }

}
