package com.example.bharath.safev1;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

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

/**
 * Created by BHARATH on 05-Jun-17.
 */

public class Tab2 extends ListFragment implements MenuItem.OnActionExpandListener,SearchView.OnQueryTextListener {
    private Context mContext;
    DatabaseHelper myDB;
    public String contacts="";
    public JSONObject jobj;
    private ArrayList<String> conNames;
    private ArrayList<String> conNumbers;
    private ArrayList<String> filterednames;
    private ArrayList<String> filterednumbers;
    private ArrayList<String> allnumbers;
    private String onlynumbers="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        myDB=new DatabaseHelper(mContext);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2, container, false);
        contacts=Tab1.getdata();
        jobj=Tab1.getjsonobj();
        try {
            jobj.getJSONArray("data");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (contacts.length() > 0) {
            new SendJsonDataToServer().execute(contacts);
        }
        allnumbers=Tab1.getcontacts();
        onlynumbers=Tab1.numbersinfo();
        conNames = new ArrayList<String>();
        conNumbers = new ArrayList<String>();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem sync=menu.findItem(R.id.sync);
        sync.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Tab2.this.sendcontacts();
                return true;
            }
        });
        MenuItem searchItem = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");

    }

    public void sendcontacts(){

    }


    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }


    private class SendJsonDataToServer extends AsyncTask<String,String,String> {
        private static final String TAG = "";

        @Override
        protected String doInBackground(String... params) {
            String JsonDATA = params[0];
            HttpURLConnection urlConnection = null;
            URL url = null;
            try {
                if (isAdded())
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
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                final String JsonResponse = convertStreamToString(in);
                Toast.makeText(mContext,"vals rx",Toast.LENGTH_SHORT).show();
            }
            catch (IOException e) {
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

}
