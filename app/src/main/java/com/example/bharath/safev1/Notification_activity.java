package com.example.bharath.safev1;

import android.app.ListActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class Notification_activity extends ListActivity {
    private ArrayList<String> conNames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_activity);
    }


}
