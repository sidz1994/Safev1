package com.example.bharath.safev1;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//import android.support.v7.widget.SearchView;

/**
 * Created by BHARATH on 05-Jun-17.
 */

public class Tab1 extends ListFragment implements SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {

    private ArrayList<String> conNames;
    private ArrayList<String> conNumbers;
    public static ArrayList<String> sendnumbers;
    public static String onlynumber="";
    public StringBuilder sb;
    private ArrayList<String> filterednames;
    private ArrayList<String> filterednumbers;
    private Cursor crContacts;
    private Context mContext;
    public static String Data="";
    public static JSONObject jobj1;
    DatabaseHelper myDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        myDB=new DatabaseHelper(mContext);
    }
    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        String item = (String) listView.getAdapter().getItem(position);
        if (getActivity() instanceof OnItem1SelectedListener) {
            ((OnItem1SelectedListener) getActivity()).OnItem1SelectedListener(item);
        }
        getFragmentManager().popBackStack();
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public static String getdata(){ return Data; }
    public static JSONObject getjsonobj(){return jobj1;}
    public static ArrayList<String> getcontacts(){return sendnumbers;}
    public static String numbersinfo (){return onlynumber;}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1, container, false);
        conNames = new ArrayList<String>();
        conNumbers = new ArrayList<String>();
        HashMap<String,String> hm=new HashMap<String, String>();
        crContacts = ContactHelper.getContactCursor(mContext.getContentResolver(), "");
        crContacts.moveToFirst();
        String number ="";
        JSONObject jobj ;
        JSONArray arr = new JSONArray();
        StringBuilder sb = new StringBuilder();
        while (!crContacts.isAfterLast()) {
            number=crContacts.getString(2).replaceAll("[()\\s-]+", "");
            if(!hm.containsKey(number)){
                hm.put(crContacts.getString(2).replaceAll("[()\\s-]+", ""),crContacts.getString(1).replaceAll("[()\\s-]+", ""));
                conNames.add(crContacts.getString(1));
                conNumbers.add(crContacts.getString(2).replaceAll("[()\\s-]+", ""));
                jobj = new JSONObject();
                try {
                    jobj.put("name",crContacts.getString(1));
                    jobj.put("number",crContacts.getString(2).replaceAll("[()\\s-]+", ""));
                    sb.append(crContacts.getString(2).replaceAll("[()\\s-]+", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                arr.put(jobj);
            }
            crContacts.moveToNext();
        }
        jobj = new JSONObject();
        try {
            jobj.put("table","contacts");
            jobj.put("data", arr);
            Data=jobj.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendnumbers=conNumbers;
        jobj1=jobj;
        onlynumber= sb.toString();
        setListAdapter(new MyAdapter(getContext(), android.R.layout.simple_list_item_1,
                R.id.tvNameMain, conNames));

        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search");
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText == null || newText.trim().isEmpty()) {
            resetSearch();
            return false;
        }

        filterednames=new ArrayList<>(conNames);
        filterednumbers=new ArrayList<>(conNumbers);
        for(String value:conNames){
            if(!value.toLowerCase().contains(newText.toLowerCase())){
                int i =filterednames.indexOf(value);
                filterednames.remove(i);  //changed from value to i
                filterednumbers.remove(i);
            }
        }
        setListAdapter(new NewAdapter(mContext, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, filterednames));
        return false;
    }
    public void resetSearch(){
        setListAdapter(new MyAdapter(mContext, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, conNames));
    }

    private class NewAdapter extends ArrayAdapter<String> {

        public NewAdapter(Context context, int resource, int textViewResourceId,
                          ArrayList<String> filterednames) {
            super(context, resource, textViewResourceId, filterednames);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = setList(position, parent);
            return row;
        }

        private View setList(int position, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = inf.inflate(R.layout.liststyle, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.tvNameMain);
            TextView tvNumber = (TextView) row.findViewById(R.id.tvNumberMain);

            tvName.setText(filterednames.get(position));
            tvNumber.setText("No: " + filterednumbers.get(position));

            return row;
        }

    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        return true;
    }

    public interface OnItem1SelectedListener {
        void OnItem1SelectedListener(String item);
    }




    private class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context context, int resource, int textViewResourceId,
                         ArrayList<String> conNames) {
            super(context, resource, textViewResourceId, conNames);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = setList(position, parent);
            return row;
        }

        private View setList(int position, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = inf.inflate(R.layout.liststyle, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.tvNameMain);
            TextView tvNumber = (TextView) row.findViewById(R.id.tvNumberMain);

            tvName.setText(conNames.get(position));
            tvNumber.setText("No: " + conNumbers.get(position));

            return row;
        }

    }

    @Override
    public void onStart(){
        super.onStart();
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder altdial=new AlertDialog.Builder(mContext);
                if (parent.getAdapter() instanceof MyAdapter)
                {
                    altdial.setMessage("Do you want to add "+conNames.get(position)+" to trusted contacts?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*boolean isInserted= myDB.insertData(conNames.get(position),conNumbers.get(position));
                                    if(isInserted){
                                        Toast.makeText(mContext,conNames.get(position)+" added to trusted contacts" ,Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(mContext,conNames.get(position)+" not added to trusted contacts" ,Toast.LENGTH_SHORT).show();
                                    }*/
                                    myDB.insertdataonce(conNames.get(position),conNumbers.get(position));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                }

                if (parent.getAdapter() instanceof NewAdapter)
                {
                    altdial.setMessage("Do you want to add "+filterednames.get(position)+" to trusted contacts?").setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    /*boolean isInserted= myDB.insertData(filterednames.get(position),filterednumbers.get(position));
                                    if(isInserted){
                                        Toast.makeText(mContext,filterednames.get(position)+" added to trusted contacts" ,Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(mContext,filterednames.get(position)+" not added to trusted contacts" ,Toast.LENGTH_SHORT).show();
                                    }*/
                                    myDB.insertdataonce(filterednames.get(position),filterednames.get(position));
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                }

                AlertDialog alert=altdial.create();
                alert.setTitle("ADD CONTACTS");
                alert.show();
            }
        });
    }

    //hide specific menu items in layout
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.sync);
        item.setVisible(false);
    }
}
