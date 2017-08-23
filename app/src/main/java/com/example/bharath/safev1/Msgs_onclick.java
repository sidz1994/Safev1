package com.example.bharath.safev1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Msgs_onclick extends AppCompatActivity {
    ImageButton can_help,cannot_help;
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
            can_help=(ImageButton) findViewById(R.id.thumbsup);
            can_help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Thanks for reaching out",Toast.LENGTH_SHORT).show();
                }
            });
            cannot_help=(ImageButton) findViewById(R.id.thumbsdown);
            cannot_help.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Sorry to hear that you cannot reach to help.",Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
