package com.example.bharath.safev1;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class Notifications_Database extends SQLiteOpenHelper {
    private static final String database_name = "notifications.db";
    private static final String n_table = "notification";


    public Notifications_Database(Context context) {
        super(context, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+n_table+" (uid text,name text,msg text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+n_table);
        onCreate(db);
    }


    public void insertdata(String uid,String name,String msg){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("insert or replace into notification (uid, name, msg) values ('"+uid+"','"+name+"','"+msg+"');");
    }
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+n_table,null);
        return res;
    }

    private int deleteData () {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(n_table, null,null);
    }
    public int  deletemsg (int rowid) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(n_table,  "rowid="+rowid,null);
    }
}
