package com.example.bharath.safev1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static com.example.bharath.safev1.R.id.map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,PlaceSelectionListener,
        View.OnClickListener,LocationListener, NavigationView.OnNavigationItemSelectedListener,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Handler handler;
    private GoogleMap mMap;
    private Gpstracker gpsTracker;
    private Location mLocation;
    double latitude, longitude;
    private GoogleApiClient mgoogleapiclient;//july 9th for onlocationchanged code
    private LocationRequest mlocrequest;//july 9th
    MyReceiver myReceiver;
    private FloatingActionButton mLocationFAB;
    Button alert,post;
    String postlat,postlong,Uid;
    ArrayList<HashMap<String, String>> contactList;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private static final int MY_PERMISSION_REQUEST_READ_CONTACTS=10;
    Profile_Database profileDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

       //set background color white for auto place fill fragment
        android.app.Fragment firstFrag = getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        firstFrag.getView().setBackgroundColor(Color.WHITE);

        //code for registering phone and assigning userid
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            startActivity(new Intent(MapsActivity.this, RegsiterActivity.class));
        }
        if(user!=null){Uid=user.getUid();}

        //PERMISSIONS TO READ CONTACTS

        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {android.Manifest.permission.READ_CONTACTS}, MY_PERMISSION_REQUEST_READ_CONTACTS);
        }

        //main activity starts here after registration
        gpsTracker = new Gpstracker(getApplicationContext());
        while(!gpsTracker.canGetLocation()) {
            gpsTracker.showSettingsAlert();
        }

        //floating action button
        mLocationFAB = (FloatingActionButton) findViewById(R.id.myLocationButton);
        mLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle GPS position updates
                movetomylocation();
            }
        });

        mLocation = gpsTracker.getLocation();
        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();
        alert = (Button) findViewById(R.id.Alert);
        alert.setOnClickListener(this);
        alert.setEnabled(true);

        contactList = new ArrayList<>();
        profileDB=new Profile_Database(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);


        //below lines are added to send token to firebase
        SharedPreferences sharedPreferences=getApplicationContext().getSharedPreferences(getString(R.string.FCM_PREF), Context.MODE_PRIVATE);
        String ff = FirebaseInstanceId.getInstance().getToken();
        final String token=sharedPreferences.getString(getString(R.string.FCM_TOKEN),ff);
        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table" , "fcm_token");
            jsonObjectobj.put("uid" , Uid);
            jsonObjectobj.put("fcm_token" , token);
        }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }
        //Toast.makeText(this,token,Toast.LENGTH_SHORT).show();//just to display the token
        /*StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params=new HashMap<String,String>();
                params.put("fcm_token",token);
                return params;
            }
        };
        MySingleton.getmInstance(MapsActivity.this).addToRequest(stringRequest);*/
        //send token to server code till here*/


        //token using broadcast receiver
        /*Intent intent = new Intent(this, FcmInstanceIdService.class);
        startService(intent);
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FcmInstanceIdService.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);*/

        //july 9th
        mgoogleapiclient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();

        //all users main table fill onec
        jsonObjectobj = new JSONObject();  //working code
        try {
            jsonObjectobj.put("table" , "location");
            jsonObjectobj.put("uid" , Uid);
            jsonObjectobj.put("lat" , String.valueOf(latitude));
            jsonObjectobj.put("long" , String.valueOf(longitude));
        }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_READ_CONTACTS:
                /*if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    readContacts();
                }*/
                //else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_CONTACTS)){
                    new AlertDialog.Builder(this).
                            setTitle("Read Contacts Permission").
                            setMessage("You need to grant read contacts permission to read contacts feature. Retry and grant it!").show();
                }
                else {
                    new AlertDialog.Builder(this).
                            setTitle("Read Contacts permission denied!").
                            setMessage("You denied read ontacts permission. So, this feature will be disabled. To engage it, go on settings and grant read contacts permission for the application").show();

                }
                // }
        }
    }


    public void movetomylocation(){
        LatLng myloc = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(myloc).title("Current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng myloc = new LatLng(latitude, longitude);
        postlat = String.valueOf(latitude);
        postlong = String.valueOf(longitude);
        mMap.addMarker(new MarkerOptions().position(myloc).title("Current location"));
         if(getIntent().getExtras()!=null){
            String newString="";
            Bundle extras = getIntent().getExtras();
            newString= extras.getString("message");
             if(newString!=null){
                 String[] uservals = newString.split(",");
                 LatLng victimloc = new LatLng(Double.parseDouble(uservals[0]), Double.parseDouble(uservals[1]));
                 mMap.addMarker(new MarkerOptions().position(victimloc).title("help needed here.."));
             }
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));
    }


    @Override
    public void onPlaceSelected(Place place) {
        mMap.clear();
        String userlatlng=String.valueOf(place.getLatLng());
        String requiredString = userlatlng.substring(userlatlng.indexOf("(") + 1, userlatlng.indexOf(")"));
        String[] uservals = requiredString.split(",");
        postlat=uservals[0];
        postlong=uservals[1];
        LatLng myloc = new LatLng(Double.parseDouble(uservals[0]), Double.parseDouble(uservals[1]));
        mMap.addMarker(new MarkerOptions().position(myloc).title("I'm here.."));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));
        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table" , "heat_map");
            jsonObjectobj.put("lat" , String.valueOf(postlat));
            jsonObjectobj.put("long" , String.valueOf(postlong));
        }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }
    }

    @Override
    public void onError(Status status) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Alert:
                alertnearby();
                break;

            default:
                break;

        }
    }


    public void alertnearby(){
        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table" , "alert");
            jsonObjectobj.put("uid" , Uid);
            jsonObjectobj.put("lat" , String.valueOf(postlat));
            jsonObjectobj.put("long" , String.valueOf(postlong));
            }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }
    }
//july 9th code below 3

    @Override
    protected void onStart() {
        super.onStart();
        mgoogleapiclient.connect();
        AppIndex.AppIndexApi.start(mgoogleapiclient, getIndexApiAction());
    }


    @Override
    protected void onStop() {
        mgoogleapiclient.disconnect();
        super.onStop();
        AppIndex.AppIndexApi.end(mgoogleapiclient, getIndexApiAction());
    }
    public Action getIndexApiAction()
    {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    //below 5 are added here on july 9th

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(Bundle bundle) {
        mlocrequest = LocationRequest.create();
        mlocrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocrequest.setInterval(1000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 108);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient, mlocrequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location){
        double dist1=geodistance(mLocation.getLatitude(),mLocation.getLongitude(),location.getLatitude(),location.getLongitude());
        if(dist1>0.1){
            JSONObject jsonObjectobj = new JSONObject();  //working code
            try {
                jsonObjectobj.put("table" , "location");
                jsonObjectobj.put("uid" , Uid);
                jsonObjectobj.put("lat" , String.valueOf(latitude));
                jsonObjectobj.put("long" , String.valueOf(longitude));
            }
            catch (JSONException e) {
                Log.d("JWP","Can't format JSON");
            }
            if (jsonObjectobj.length() > 0) {
                new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
            }

        }
        //Toast.makeText(this,String.valueOf(latitude)+" "+String.valueOf(longitude),Toast.LENGTH_SHORT).show();
    }



    private double geodistance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private class SendJsonDataToServer extends AsyncTask<String,String,String> {
        private static final String TAG = "";

        @Override
        protected String doInBackground(String... params) {
            //String JsonResponse = null;
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            URL url = null;
            try {
                //url = new URL("http://ec2-13-59-101-206.us-east-2.compute.amazonaws.com:4000/");
                url = new URL("http://192.168.0.11:4000/");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            Log.i("xnxxx",url.toString());//not required
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
                OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                out.write(JsonDATA.getBytes());
                out.flush();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final String JsonResponse = convertStreamToString(in);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),JsonResponse,Toast.LENGTH_LONG).show();
                    }
                });
                Log.i("xnxxx","writr closed");//not required
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                    Log.i("xnxxx","disoconeec");//not required
                }
            }
            return null;
        }




    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }


    protected Marker createMarker(double latitude, double longitude, String name) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(name)
                );
    }


    //this code is for navigation menu
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            int profile_counts = profileDB.getProfilesCount();
            if(profile_counts!=0){
            Intent profile_intent=new Intent(MapsActivity.this,Profile_view.class);
            startActivity(profile_intent);
            }
            else{
                Intent profile_intent=new Intent(MapsActivity.this,Edit_profile_json_test.class);//changed here
                profile_intent.putExtra("uid",Uid);
                startActivity(profile_intent);
                }
        } else if (id == R.id.nav_contacts) {
            Intent contacts_intent=new Intent(MapsActivity.this,Contacts_Activity_Main.class);
            startActivity(contacts_intent);

        } else if (id == R.id.nav_logout) {
            profileDB.deleteData();
            Intent refresh_intent=new Intent(MapsActivity.this,MapsActivity.class);
            startActivity(refresh_intent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //recieves fcm_token everytime it changes and sends to server
    public class MyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String fcm_token = intent.getStringExtra("fcm_token");
            JSONObject jsonObjectobj = new JSONObject();
            try {
                jsonObjectobj.put("table" , "fcm_token");
                jsonObjectobj.put("uid" , Uid);
                jsonObjectobj.put("fcm_token" , fcm_token);
            }
            catch (JSONException e) {
                Log.d("JWP","Can't format JSON");
            }
            if (jsonObjectobj.length() > 0) {
                new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
            }
        }
    }

  /*  @Override
    public void onLowMemory() {
        super.onLowMemory();

        gpsTracker.onLowMemory();
    }
*/


}//on nsvigation end
