package com.example.bharath.safev1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String database_name="contacts.db";
    public static final String contacts_table="trusted_contacts";
    public static final String contacts_COL1="name";
    public static final String contacts_COL2="number";
    public DatabaseHelper(Context context) {
        super(context, database_name, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+contacts_table+" (name text,number text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+contacts_table);
        onCreate(db);
    }
    public boolean insertData(String name,String number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(contacts_COL1,name);
        contentValues.put(contacts_COL2,number);
        long result = db.insert(contacts_table,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+contacts_table,null);
        return res;
    }

    public Integer deleteData (String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(contacts_table, "name = ?",new String[] {name});
    }

    public Cursor getAllDatacursor() {
        String selectQuery = "Select * from "+contacts_table;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        return cursor;
    }

  }
