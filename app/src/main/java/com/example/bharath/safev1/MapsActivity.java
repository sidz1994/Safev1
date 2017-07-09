package com.example.bharath.safev1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
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

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.bharath.safev1.R.id.map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,PlaceSelectionListener,
        View.OnClickListener,LocationListener, NavigationView.OnNavigationItemSelectedListener{

    private GoogleMap mMap;
    private Gpstracker gpsTracker;
    private Location mLocation;
    double latitude, longitude;

    Button alert,post;
    String result="";
    String postlat,postlong,Uid;
    public static final String TAG = "YOUR-TAG-NAME";
    String strurl = "http://www.telusko.com/addition.htm?t1=3&t2=-779";
    String newurl="http://127.0.0.1:8000/location/updateLoc/user_id=uid&latlong=27,111";

    //ArrayList<MarkerData> markersArray = new ArrayList<MarkerData>();
    ArrayList<HashMap<String, String>> contactList;
    String app_server_url="http://10.0.0.16:801/safe/fcm_insert.php";

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

            mLocation = gpsTracker.getLocation();
            latitude = mLocation.getLatitude();
            longitude = mLocation.getLongitude();


        //mLocation = gpsTracker.getLocation();
        alert = (Button) findViewById(R.id.Alert);
        alert.setOnClickListener(this);
        alert.setEnabled(true);
        post = (Button) findViewById(R.id.post);
        post.setOnClickListener(this);
        post.setEnabled(true);
        //pitre...turn on gps in phone and give permissions in settings later add code for it
       // latitude = mLocation.getLatitude();
        //longitude = mLocation.getLongitude();
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
        //Toast.makeText(this,token,Toast.LENGTH_SHORT).show();//just to display the token
        StringRequest stringRequest = new StringRequest(Request.Method.POST, app_server_url,
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
        MySingleton.getmInstance(MapsActivity.this).addToRequest(stringRequest);
        //send token to server code till here

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
                 LatLng vitimloc = new LatLng(Double.parseDouble(uservals[0]), Double.parseDouble(uservals[1]));
                 mMap.addMarker(new MarkerOptions().position(vitimloc).title("help needed here.."));
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
    }

    @Override
    public void onError(Status status) {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Alert:
                sendatatoserver();
                break;
            case R.id.post:
                postData();
                break;
            default:
                break;

        }
    }


    public void postData(){
        int templat= (int) Math.ceil(Double.parseDouble(String.valueOf(postlat)));
        int templong= (int) Math.ceil(Double.parseDouble(String.valueOf(postlong)))*-1;
        newurl ="http://127.0.0.1:8000/location/updateLoc?user_id="+Uid+"&latlong="+templat+","+templong ;
        new sendhttpdata().execute(newurl);


    }

    public void sendatatoserver(){
        int templat= (int) Math.ceil(Double.parseDouble(String.valueOf(postlat)));
        int templong= (int) Math.ceil(Double.parseDouble(String.valueOf(postlong)))*-1;
        newurl ="http://127.0.0.1:8000/location/updateLoc?user_id="+Uid+"&latlong="+templat+","+templong ;
                //"http://www.telusko.com/addition.htm?t1="+templat+"&t2="+templong;
        new SendJsonDataToServer().execute(newurl);
        mMap.clear();
        for(int i = 0 ; i < contactList.size() ; i++ ) {

            createMarker(Double.parseDouble(contactList.get(i).get("Lat")), Double.parseDouble(contactList.get(i).get("Long")),contactList.get(i).get("Name"));
        }
    }
    protected Marker createMarker(double latitude, double longitude, String name) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(name)
                );
    }

    @Override
    public void onLocationChanged(Location location) {

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


    private class sendhttpdata extends AsyncTask<String,String,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(String... params) {

            String urlString = params[0]; // URL to call

            String resultToDisplay = "";

            InputStream in = null;
            try {

                URL url = new URL(urlString);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.connect();
                in = new BufferedInputStream(urlConnection.getInputStream());


            } catch (Exception e) {

                System.out.println(e.getMessage());

                return e.getMessage();

            }

            try {
                resultToDisplay = IOUtils.toString(in, "UTF-8");
                //to [convert][1] byte stream to a string
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return resultToDisplay;
        }


        @Override
        protected void onPostExecute(String result) {
            //Update the UI
        }
    }

    private class SendJsonDataToServer extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String s){
           // super.onPostExecute(s);
            Toast.makeText(MapsActivity.this, "act "+s, Toast.LENGTH_SHORT).show();

        }


        @Override
        protected String doInBackground(String... params) {
            try{

                URL url=new URL(params[0]);
                HttpURLConnection con=(HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();

                BufferedReader bf= new BufferedReader(new InputStreamReader(con.getInputStream()));
                String val=bf.readLine();
                System.out.println(val);
                result=val;

                //json code is added here
                HttpHandler sh = new HttpHandler();

                String jsonStr = sh.makeServiceCall(url.toString());
                Log.e(TAG, "Response from url: " + jsonStr);

                if (jsonStr != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray contacts = jsonObj.getJSONArray("contacts");

                        // looping through All Contacts
                        for (int i = 0; i < contacts.length(); i++) {
                            JSONObject c = contacts.getJSONObject(i);

                            String id = c.getString("id");
                            String Lat = c.getString("Lat");
                            String Long = c.getString("Long");
                            String name = c.getString("name");
                            String gender = c.getString("gender");
                            String age = c.getString("age");
                            String mobile = c.getString("mobile");

                            // tmp hash map for single contact
                            HashMap<String, String> contact = new HashMap<>();

                            // adding each child node to HashMap key => value
                            contact.put("id", id);
                            contact.put("name", name);
                            contact.put("mobile", mobile);
                            contact.put("latitude", Lat);
                            contact.put("longitude", Long);
                        }
                    } catch (final JSONException e) {
                        Log.e(TAG, "Json parsing error: " + e.getMessage());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "Json parsing error: " + e.getMessage(),
                                        Toast.LENGTH_LONG)
                                        .show();
                            }
                        });

                    }
                }
                else {
                    Log.e(TAG, "Couldn't get json from server.");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Couldn't get json from server. Check LogCat for possible errors!",
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }


                            //till here

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
    }
}
