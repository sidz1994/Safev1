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
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by BHARATH on 05-Jun-17.
 */

public class Tab3 extends ListFragment implements SearchView.OnQueryTextListener {
    private ArrayList<String> conNames;
    private ArrayList<String> conNumbers;
    DatabaseHelper myDB;
    private Context mContext;
    MyAdapter mAdapter=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        myDB=new DatabaseHelper(mContext);
        mAdapter=new MyAdapter(getContext(), android.R.layout.simple_list_item_1,
                R.id.tvNameMain, conNames);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3, container, false);
        conNames = new ArrayList<String>();
        conNumbers = new ArrayList<String>();
        Cursor res = myDB.getAllData();
        if(res.getCount() == 0) {
            showMessage("Message","No contacts added.");
        }

        //StringBuffer buffer = new StringBuffer();
        while (res.moveToNext()) {
            conNames.add(res.getString(0));
            conNumbers.add(res.getString(1));
        }
        setListAdapter(new MyAdapter(getContext(), android.R.layout.simple_list_item_1,
                R.id.tvNameMain, conNames));
        // showMessage("Data",buffer.toString());
        return rootView;
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        String item = (String) listView.getAdapter().getItem(position);
        if (getActivity() instanceof Tab1.OnItem1SelectedListener) {
            ((Tab1.OnItem1SelectedListener) getActivity()).OnItem1SelectedListener(item);
        }
        getFragmentManager().popBackStack();
    }

    public void showMessage(String title,String Message){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(Message);
        builder.show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
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
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder altdial=new AlertDialog.Builder(mContext);
                altdial.setMessage("Do you want to remove "+conNames.get(position)+" from trusted contacts?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(mContext,conNames.get(position)+" added to trusted contacts" ,Toast.LENGTH_SHORT).show();
                                Integer deletedRows = myDB.deleteData(conNames.get(position));
                                if(deletedRows > 0) {
                                    conNames.remove(position);
                                    mAdapter.notifyDataSetChanged();
                                    Toast.makeText(mContext, "Contact removed", Toast.LENGTH_LONG).show();
                                }
                                else
                                    Toast.makeText(mContext,"Contact not removed",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert=altdial.create();
                alert.setTitle("ADD CONTACTS");
                alert.show();
            }
        });
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

}
