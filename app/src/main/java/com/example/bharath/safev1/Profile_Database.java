package com.example.bharath.safev1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by BHARATH on 18-Jun-17.
 */

public class Profile_Database  extends SQLiteOpenHelper {
    public static final String database_name="profile.db";
    public static final String profile_table="profile";
    public static final String profile_name="name";
    public static final String profile_number="number";
    public static final String profile_email="mail";
    public static final String profile_pwd="pwd";
    public static final String profile_age="age";
    public static final String profile_blood="blood";
    public static final String profile_msg="msg";
    public static final String profile_sex="sex";
    public Profile_Database(Context context) {
        super(context, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+profile_table+" (name text,number text,mail text,pwd text,age integer,blood text,msg text,sex text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+profile_table);
        onCreate(db);
    }

    public boolean insertprofiledata(String name,String number,String mail ,String pwd ,String age ,String blood ,String msg,String sex){
        deleteData();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(profile_name, name);
        contentValues.put(profile_number, number);
        contentValues.put(profile_email, mail);
        contentValues.put(profile_pwd, pwd);
        contentValues.put(profile_age, age);
        contentValues.put(profile_blood, blood);
        contentValues.put(profile_msg, msg);
        contentValues.put(profile_sex,sex);
        long result = db.insert(profile_table,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+profile_table,null);
        return res;
    }

    public int deleteData () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(profile_table, null,null);
    }

    public int getProfilesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int cnt  = (int) DatabaseUtils.queryNumEntries(db, profile_table);
        db.close();
        return cnt;
    }

}
