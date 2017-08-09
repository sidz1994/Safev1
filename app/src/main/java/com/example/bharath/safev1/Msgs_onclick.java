package com.example.bharath.safev1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class Msgs_onclick extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msgs_onclick);
        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            TextView victim_name = (TextView)findViewById(R.id.msg_onclick_name);
            victim_name.setText(extras.getString("name"));
            TextView victim_msg = (TextView)findViewById(R.id.msg_onclick_msg);
            victim_msg.setText( extras.getString("msg"));
        }
    }
}
