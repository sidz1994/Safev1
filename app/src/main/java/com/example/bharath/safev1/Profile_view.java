package com.example.bharath.safev1;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Profile_view extends AppCompatActivity implements View.OnClickListener {
    Profile_Database profileDB;
    private TextView name, number,age,email,pwd,msg,blood,sex;
    private Button edit;
    String read_name,read_number,read_age,read_email,read_sex,read_msg,read_pwd,read_blood;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);
        profileDB=new Profile_Database(this);
        name = (TextView) findViewById(R.id.profile_view_name);
        number = (TextView) findViewById(R.id.profile_view_number);
        age = (TextView) findViewById(R.id.profile_view_age);
        email = (TextView) findViewById(R.id.profile_view_email);
        pwd = (TextView) findViewById(R.id.profile_view_pwd);
        msg = (TextView) findViewById(R.id.profile_view_msg);
        blood = (TextView) findViewById(R.id.profile_view_blood);
        sex = (TextView) findViewById(R.id.profile_view_sex);
        edit = (Button) findViewById(R.id.profile_view_edit);
        edit.setOnClickListener(this);
        edit.setEnabled(true);

        Cursor cursor=profileDB.getAllData();
        if (cursor.moveToFirst())
        {
            do
            {
                read_name=cursor.getString(0);
                read_number=cursor.getString(1);
                read_age=cursor.getString(2);
                read_email=cursor.getString(3);
                read_pwd=cursor.getString(4);
                read_blood=cursor.getString(5);
                read_msg=cursor.getString(6);
                read_sex=cursor.getString(7);
            } while (cursor.moveToNext());
        }
        cursor.close();
        name.setText(read_name);
        number.setText(read_number);
        email.setText(read_email);
        pwd.setText(read_pwd);
        age.setText(read_age);
        blood.setText(read_blood);
        msg.setText(read_msg);
        sex.setText(read_sex);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.profile_view_edit:
                Intent main_Activity=new Intent(this,Edit_profile_json_test.class);
                startActivity(main_Activity);
                break;
            default:
                break;

        }
    }
}
