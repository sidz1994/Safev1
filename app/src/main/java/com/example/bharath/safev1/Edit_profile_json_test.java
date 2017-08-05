package com.example.bharath.safev1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Edit_profile_json_test extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    //search for text "ALERT" . sOME CHANGES MIGHT BE NEEDED WHERE EVER THEY ARE MENTIONED

    private Button submit;
    private EditText name, number,age,email,pwd,msg;
    private Spinner blood,sex;
    //private Context mContext;
    Profile_Database profileDB;
    String blood_group,sex_group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileDB=new Profile_Database(this);
        setContentView(R.layout.activity_profile_edit);
        name=(EditText)findViewById(R.id.profile_name);
        number=(EditText)findViewById(R.id.profile_number);
        age=(EditText)findViewById(R.id.profile_age);
        email=(EditText)findViewById(R.id.profile_email);
        pwd=(EditText)findViewById(R.id.profile_password);
        msg=(EditText)findViewById(R.id.profile_msg);
        blood = (Spinner) findViewById(R.id.profile_blood);
        blood.setOnItemSelectedListener(this);
        // Spinner Drop down elements for blood
        List<String> categories = new ArrayList<String>();
        categories.add("---");
        categories.add("A+");
        categories.add("A-");
        categories.add("B+");
        categories.add("B-");
        categories.add("AB");
        categories.add("O+");
        categories.add("O-");
        categories.add("Don't Know");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        blood.setAdapter(dataAdapter);
        //spinner for sex
        sex = (Spinner) findViewById(R.id.profile_sex);
        sex.setOnItemSelectedListener(this);
        List<String> categories_Sex = new ArrayList<String>();
        categories_Sex.add("-----");
        categories_Sex.add("Male");
        categories_Sex.add("Female");
        categories_Sex.add("Other");
        ArrayAdapter<String> dataAdapter_sex = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories_Sex);
        dataAdapter_sex.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex.setAdapter(dataAdapter_sex);
        submit = (Button) findViewById(R.id.profile_submit);
        submit.setOnClickListener(this);
        submit.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_submit:
                load_data_database();
                senddatatoserver();
                Intent main_Activity=new Intent(this,MapsActivity.class);
                startActivity(main_Activity);
                break;
            default:
                break;

        }
    }

    public void senddatatoserver() {
        JSONObject jsonObjectobj = new JSONObject();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString("uid", null);
        try {
            jsonObjectobj.put("table" , "profile");
            jsonObjectobj.put("uid" , uid);
            jsonObjectobj.put("name" , name.getText().toString());
            jsonObjectobj.put("number", number.getText().toString());
            jsonObjectobj.put("age" , age.getText().toString());
            jsonObjectobj.put("email" , email.getText().toString());
            jsonObjectobj.put("pwd" , pwd.getText().toString());
            jsonObjectobj.put("msg" , msg.getText().toString());
            jsonObjectobj.put("blood" , blood_group);
            jsonObjectobj.put("sex" , sex_group);
        }
        catch (JSONException e) {
            Log.d("JWP","Can't format JSON");
        }
        if (jsonObjectobj.length() > 0) {
            new SendJsonDataToServer().execute(String.valueOf(jsonObjectobj));
        }}

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
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

        public void load_data_database(){

        if(check_all_values()){
            boolean isInserted= profileDB.insertprofiledata(name.getText().toString(),number.getText().toString(),email.getText().toString() ,pwd.getText().toString() ,age.getText().toString() ,blood_group,msg.getText().toString(),sex_group);
            if(isInserted){
                Toast.makeText(this,"Profile created" ,Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this,"Profile not created" ,Toast.LENGTH_SHORT).show();
            }}
    }
    public boolean check_all_values(){
        if (!(name.getText().toString().matches("") ||number.getText().toString().matches("") ||email.getText().toString().matches("") || pwd.getText().toString().matches("") || age.getText().toString().matches("") || blood_group.matches("---") ||msg.getText().toString().matches("") ||sex_group.matches("-----")))
        {
            return true;
        }
        Toast.makeText(this,"Fill all Details" ,Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.profile_blood)
        {blood_group = parent.getItemAtPosition(position).toString();}
        if (spinner.getId() == R.id.profile_sex)
        {sex_group=parent.getItemAtPosition(position).toString();}
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


}
