package com.example.winterbreak2023app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventDataBase extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "events";
    public static final String COLUMN_EVENT_NAME = "EVENT_NAME";
    public static final String COLUMN_EVENT_DESC = "EVENT_DESC";

    //Constructor for superclass SQLiteOpenHelper
    public EventDataBase(@Nullable Context context) {
        super(context, "events.db", null, 1);
    }

    //This method is executed the first time a database is called in program for initialization
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_EVENT_NAME + " TEXT NOT NULL, " + COLUMN_EVENT_DESC + " TEXT NOT NULL, UNIQUE (" + COLUMN_EVENT_NAME + ", " + COLUMN_EVENT_DESC + "))";

        db.execSQL(createTableStatement);
    }

    //This method is called every time the version number changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(String eventName, String eventDesc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_EVENT_NAME, eventName);
        cv.put(COLUMN_EVENT_DESC, eventDesc);

        long insert = db.insert(TABLE_NAME, null, cv);

        db.close();

        return insert != -1;
    }

    public boolean deleteOne(String eventName, String eventDesc) {
        SQLiteDatabase db = this.getWritableDatabase();
        //DELETE FROM TABLE_NAME WHERE COLUMN_EVENT_NAME = 'eventName' AND COLUMN_EVENT_DESC = 'eventDesc';
        String queryString = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_EVENT_NAME + " = \"" + eventName + "\" AND " + COLUMN_EVENT_DESC + " = \"" + eventDesc + "\"";

        Cursor cursor = db.rawQuery(queryString, null);

        boolean resultBoolean = !cursor.moveToFirst();
        cursor.close();
        db.close();
        return resultBoolean;
    }

    public ArrayList<String[]> getAllEvents() {
        ArrayList<String[]> events = new ArrayList<>();

        String queryString = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()) {
            do {
                String eventName = cursor.getString(0);
                String eventDesc = cursor.getString(1);

                String[] newEvent = {eventDesc, eventName};
                events.add(newEvent);
            } while(cursor.moveToNext());
        }

        //Close both cursor and database once the connection is complete
        cursor.close();
        db.close();
        return events;
    }
}
