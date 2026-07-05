package com.example.hershield;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FeedbackDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hersheild.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "Feedback";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TEXT = "text";

        public FeedbackDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_RATING + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TEXT + " TEXT"+")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public boolean addFeedback(String rating,String name,String text)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(COLUMN_RATING,rating);
        values.put(COLUMN_NAME,name);
        values.put(COLUMN_TEXT,text);
        long result=db.insert(TABLE_NAME,null,values);
        db.close();
        return result!=-1;//true if inserted successfully

    }
}

