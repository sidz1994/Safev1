package com.example.bharath.safev1;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class FcmInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh(){
        String recent_token= FirebaseInstanceId.getInstance().getToken();

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.Uid),Context.MODE_PRIVATE);
        String ff = getResources().getString(R.string.Uid);
        final String Uid=sharedPref.getString(getString(R.string.Uid),ff);



        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table" , "fcm_token");
            jsonObjectobj.put("uid" , Uid);
            jsonObjectobj.put("fcm_token" , recent_token);
        }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {


            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                url = new URL(getString(R.string.URL));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            try {
                assert url != null;
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                assert urlConnection != null;
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(jsonObjectobj.toString().getBytes());
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }
    }
}
