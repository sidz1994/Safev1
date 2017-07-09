package com.example.bharath.safev1;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by BHARATH on 05-Jun-17.
 */

public class Tab2 extends Fragment {
    private Context mContext;
    DatabaseHelper myDB;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        setHasOptionsMenu(true);
        myDB=new DatabaseHelper(mContext);
        String contacts=getArguments().getString("Contacts_json");
        System.out.println(contacts);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2, container, false);
        return rootView;
    }

}
