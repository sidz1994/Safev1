package com.example.bharath.safev1;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Notification_activity extends ListActivity {
    private ArrayList<String> victim_Names= new ArrayList<String>();
    private ArrayList<String> victim_msg= new ArrayList<String>();
    private Context mContext;
    Notifications_Database myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        try {
            myDB = new Notifications_Database(mContext);
            Cursor cursor = myDB.getAllData();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        victim_Names.add(cursor.getString(cursor.getColumnIndex("name")));
                        victim_msg.add(cursor.getString(cursor.getColumnIndex("msg")));
                    } while (cursor.moveToNext());
                }
            }
            else
                Toast.makeText(this,"No Messages",Toast.LENGTH_SHORT).show();

        }
        catch (SQLiteException se ) {
            Log.e(getClass().getSimpleName(), "Could not create or Open the database");
        } finally {
            myDB.close();
        }
        setListAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, victim_Names));

    }


    private class MyAdapter extends ArrayAdapter<String> {

        private MyAdapter(Context context, int resource, int textViewResourceId,
                         ArrayList<String> conNames) {
            super(context, resource, textViewResourceId, conNames);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return setList(position, parent);
        }

        private View setList(int position, ViewGroup parent) {
            LayoutInflater inf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = inf.inflate(R.layout.liststyle, parent, false);

            TextView tvName = (TextView) row.findViewById(R.id.tvNameMain);
            TextView tvNumber = (TextView) row.findViewById(R.id.tvNumberMain);

            tvName.setText(victim_Names.get(position));
            tvNumber.setText(victim_msg.get(position));

            return row;
        }

    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        Intent intent =new Intent(this, Msgs_onclick.class);
        Bundle bundle=new Bundle();
        bundle.putString("name",victim_Names.get(position));
        bundle.putString("msg",victim_msg.get(position));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
