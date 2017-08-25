package com.example.bharath.safev1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Notification_activity extends AppCompatActivity {
    private ArrayList<String> victim_Names= new ArrayList<String>();
    private ArrayList<String> victim_msg= new ArrayList<String>();
    private Context mContext;
    Notifications_Database myDB;
    private MyAdapter arrayAdapter;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        setContentView(R.layout.noti_layout);
        arrayAdapter = new MyAdapter(this, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, victim_Names);
        listView=(ListView)findViewById(R.id.list);
        listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long id) {
                final String name=victim_Names.get(position);
                AlertDialog.Builder altdial=new AlertDialog.Builder(Notification_activity.this);
                altdial.setMessage("Do you want to delete "+name+"'s message?").setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int vals= myDB.deletemsg(name);
                                if(vals > 0) {
                                    victim_Names.remove(position);
                                    arrayAdapter.notifyDataSetChanged();
                                    Toast.makeText(mContext, "Message deleted removed", Toast.LENGTH_LONG).show();
                                    listView.setAdapter(new MyAdapter(mContext, android.R.layout.simple_list_item_1,
                                            R.id.tvNameMain, victim_Names));
                                }
                                else
                                    Toast.makeText(mContext,"Message not deleted",Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert=altdial.create();
                alert.setTitle("Delete Message");
                alert.show();
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent =new Intent(Notification_activity.this, Msgs_onclick.class);
                Bundle bundle=new Bundle();
                bundle.putString("name",victim_Names.get(position));
                bundle.putString("msg",victim_msg.get(position));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        getlistview();


    }

    private void getlistview(){
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

        /*listView.setAdapter(new MyAdapter(this, android.R.layout.simple_list_item_1,
                R.id.tvNameMain, victim_Names));*/
        listView.setAdapter(arrayAdapter);

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

}
