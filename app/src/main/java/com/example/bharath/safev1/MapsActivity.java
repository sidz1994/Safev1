package com.example.bharath.safev1;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;
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


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        View.OnClickListener, LocationListener, NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Handler handler;
    private GoogleMap mMap;
    private Gpstracker gpsTracker;
    private Location mLocation;
    double latitude, longitude, prev_lat, prev_long;
    private GoogleApiClient mgoogleapiclient;//july 9th for onlocationchanged code
    private LocationRequest mlocrequest;//july 9th
    MyReceiver myReceiver;
    private FloatingActionButton mLocationFAB;
    Button alert;
    String postlat, postlong, Uid;
    ArrayList<HashMap<String, String>> contactList;
    String victim_name = "";
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private static final int MY_PERMISSION_REQUEST_READ_CONTACTS = 10;
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 11;
    private static final int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 12;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    Profile_Database profileDB;
    //private ArrayList<Marker> mMarkerArray = new ArrayList<Marker>();
    JSONObject jsonObjectobj;
    //private boolean onplaceselected = false;
    private ProgressBar progressBar;
    AlertDialog Dialog;

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
        View hview = navigationView.getHeaderView(0);
        TextView nav_user = (TextView) hview.findViewById(R.id.nav_name);
        TextView nav_mail = (TextView) hview.findViewById(R.id.nav_email);
        profileDB = new Profile_Database(this);
        Cursor cursor = profileDB.getAllData();
        if (cursor.moveToFirst()) {
            do {
                nav_user.setText(cursor.getString(0));
                nav_mail.setText(cursor.getString(3));
            } while (cursor.moveToNext());
        }
        cursor.close();
        //set background color white for auto place fill fragment
        android.app.Fragment firstFrag = getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        firstFrag.getView().setBackgroundColor(Color.WHITE);

        //code for registering phone and assigning userid
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
      /*  if (user == null) {
            startActivity(new Intent(MapsActivity.this, RegsiterActivity.class));
            finish();
        }
        if (user != null) {
            Uid = user.getUid();
        }
*/
        //PERMISSIONS TO READ CONTACTS

        getpermissions();

        //main activity starts here after registration
        alert = (Button) findViewById(R.id.Alert);
        alert.setOnClickListener(this);
        alert.setEnabled(true);

        contactList = new ArrayList<>();
        profileDB = new Profile_Database(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(this);


        //below lines are added to send token to firebase
        String token = FirebaseInstanceId.getInstance().getToken();
        jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table", "fcm_token");
            jsonObjectobj.put("uid", Uid);
            jsonObjectobj.put("fcm_token", token);
        } catch (JSONException e) {
            Log.d("JWP", "Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("uid", Uid);
        editor.apply();

        //july 9th
        mgoogleapiclient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();

        gpsTracker = new Gpstracker(this);
        //floating action button
        mLocationFAB = (FloatingActionButton) findViewById(R.id.myLocationButton);
        mLocationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                movetomylocation();
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar_maps);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_CONTACTS:
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    new AlertDialog.Builder(this).
                            setTitle("Read Contacts Permission").
                            setMessage("You need to grant read contacts permission to read contacts feature. Retry and grant it!").show();
                }

            case MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION:
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    new AlertDialog.Builder(this).
                            setTitle("Location Fine Permission").
                            setMessage("You need to grant read contacts permission to read contacts feature. Retry and grant it!").show();
                }


            case MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION:
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    new AlertDialog.Builder(this).
                            setTitle("Location Coarse Permission").
                            setMessage("You need to grant read contacts permission to read contacts feature. Retry and grant it!").show();
                }
        }
    }


    public void movetomylocation() {
        mMap.clear();
        if (gpsTracker.canGetLocation()) {
            mLocation = gpsTracker.getLocation();
            if (mLocation != null) {
                LatLng myloc = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (!gpsTracker.isGPSEnabled) {
            show_map_when_gps_turned_off();
        } else {
            mLocation = gpsTracker.getLocation();
            if (mLocation != null) {
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
                JSONObject jsonObjectobj = new JSONObject();  //working code
                try {
                    jsonObjectobj.put("table", "location");
                    jsonObjectobj.put("uid", Uid);
                    jsonObjectobj.put("lat", String.valueOf(latitude));
                    jsonObjectobj.put("long", String.valueOf(longitude));
                } catch (JSONException e) {
                    Log.d("JWP", "Can't format JSON");
                }
                if (jsonObjectobj.length() > 0) {
                    new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        LatLng myloc = new LatLng(latitude, longitude);
        postlat = String.valueOf(latitude);
        postlong = String.valueOf(longitude);
        //Marker marker = null;
/*        Marker marker= mMap.addMarker(new MarkerOptions().position(myloc).title("Current location"));
        mMarkerArray.add(marker);*/
        if (getIntent().getExtras() != null) {
            String msg_String = "";
            String name_String = "";
            Bundle extras = getIntent().getExtras();
            msg_String = extras.getString("message");
            name_String = extras.getString("name");
            if (name_String != null) {
                String[] uservals = name_String.split(",");
                victim_name = uservals[0];
            }
            if (msg_String != null) {
                String locvals = msg_String.substring(msg_String.lastIndexOf(":") + 1);
                String[] uservals = locvals.split(",");
                LatLng victimloc = new LatLng(Double.parseDouble(uservals[0]), Double.parseDouble(uservals[1]));
                if (victim_name.isEmpty()) {
                    mMap.addMarker(new MarkerOptions().position(victimloc).title("help needed here.."));
                } else
                    mMap.addMarker(new MarkerOptions().position(victimloc).title(victim_name));

            }
            //mMarkerArray.add(marker);


            /*LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker markers : mMarkerArray) {
                builder.include(markers.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.moveCamera(cu);*/
        }
        Log.i("xnxx", "in mapready");
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));

        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //UiSettings.setZoomControlsEnabled(true);

    }


    @Override
    public void onPlaceSelected(Place place) {
        mMap.clear();
        //onplaceselected=true;
        String userlatlng = String.valueOf(place.getLatLng());
        String requiredString = userlatlng.substring(userlatlng.indexOf("(") + 1, userlatlng.indexOf(")"));
        String[] uservals = requiredString.split(",");
        postlat = uservals[0];
        postlong = uservals[1];
        LatLng myloc = new LatLng(Double.parseDouble(uservals[0]), Double.parseDouble(uservals[1]));
        mMap.addMarker(new MarkerOptions().position(myloc).title("I'm here.."));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));
        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table", "heat_map");
            jsonObjectobj.put("lat", String.valueOf(postlat));
            jsonObjectobj.put("long", String.valueOf(postlong));
        } catch (JSONException e) {
            Log.d("JWP", "Can't format JSON");
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


    public void alertnearby() {
        JSONObject jsonObjectobj = new JSONObject();
        try {
            jsonObjectobj.put("table", "alert");
            jsonObjectobj.put("uid", Uid);
            jsonObjectobj.put("lat", String.valueOf(mLocation.getLatitude()));
            jsonObjectobj.put("long", String.valueOf(mLocation.getLongitude()));
        } catch (JSONException e) {
            Log.d("JWP", "Can't format JSON");
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

    public Action getIndexApiAction() {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 108);
        Location location = LocationServices.FusedLocationApi.getLastLocation(mgoogleapiclient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient, mlocrequest, this);
        }

       // LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient, mlocrequest, this);
    }

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        mlocrequest = LocationRequest.create();
        mlocrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocrequest.setInterval(1000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 108);
            //return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mgoogleapiclient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleapiclient, mlocrequest, this);
        }
        }*/

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
   /*     if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("loc error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
*/    }
    @Override
    public void onLocationChanged(Location location){
        if(mLocation!=null)
        {
            latitude=mLocation.getLatitude();
            longitude=mLocation.getLongitude();

            if(prev_lat==0.0){
                prev_lat=latitude;
                prev_long=longitude;
            }

            double dist1=geodistance(latitude,longitude,prev_lat,prev_long);
            if(dist1>0.01){
                prev_lat=latitude;
                prev_long=longitude;
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
        }
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
            String JsonDATA = params[0];
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
                out.write(JsonDATA.getBytes());
                out.flush();
                if(JsonDATA.contains("heat_map"))
                {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    final String JsonResponse = convertStreamToString(in);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),JsonResponse+" contacts are near your location",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
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
                sb.append(line);
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
            //profileDB.deleteData();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MapsActivity.this,RegsiterActivity.class));
            finish();
        } else if (id == R.id.nav_notification) {
            Intent notifi_intent=new Intent(MapsActivity.this,Notification_activity.class);
            startActivity(notifi_intent);
        } else if (id == R.id.nav_settings) {
            Intent settings_intent=new Intent(MapsActivity.this,Settings.class);
            settings_intent.putExtra("uid",Uid);
            startActivity(settings_intent);
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

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        gpsTracker.onLowMemory();
    }


    public void getpermissions()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_CONTACTS}, MY_PERMISSION_REQUEST_READ_CONTACTS);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
        }

    }


    public void show_map_when_gps_turned_off(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        Dialog = alertDialog.create();
            alertDialog.setTitle("GPS settings");
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    if (Dialog != null && Dialog.isShowing()) {
                        Dialog.dismiss();
                    }
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    gpsTracker = new Gpstracker(getApplicationContext());
                    dialog.cancel();
                    boolean state=true;
                    progressBar.setVisibility(View.VISIBLE);
                    while (state){
                        if(gpsTracker.getLocation()!=null)
                            state=false;
                    }
                    mLocation = gpsTracker.getLocation();
                    progressBar.setVisibility(View.GONE);
                    if (mLocation != null) {
                        latitude=mLocation.getLatitude();
                        longitude=mLocation.getLongitude();
                        LatLng myloc = new LatLng(latitude, longitude);
                        Log.i("xnxx","in end");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 15));
                    }

                }

            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        alertDialog.show();
    }

    @Override
    public void onPause(){
        super.onPause();
        if (Dialog != null && Dialog.isShowing()) {
            Dialog.dismiss();
        }
       /* if (mgoogleapiclient.isConnected()) {
            mgoogleapiclient.disconnect();
        }*/
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
       if (Dialog != null && Dialog.isShowing()) {
            Dialog.dismiss();
        }
         /* if (mgoogleapiclient.isConnected()) {
            mgoogleapiclient.disconnect();
        }*/
    }
}