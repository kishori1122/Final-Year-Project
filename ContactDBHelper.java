package com.example.hershield;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "hersheild.db";
    private static final int DATABASE_VERSION = 2; // Incremented database version to trigger table upgrade

    public static final String TABLE_NAME = "contacts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_FNAME = "fname";
    public static final String COLUMN_LNAME = "lname";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_USERNAME = "username"; // Added username column to isolate contacts

    private final Context context;

    public ContactDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FNAME + " TEXT,"
                + COLUMN_LNAME + " TEXT,"
                + COLUMN_CONTACT + " TEXT,"
                + COLUMN_USERNAME + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Helper to get currently logged in username
    private String getLoggedInUsername() {
        if (context == null) return "";
        return context.getSharedPreferences("SheShieldPrefs", Context.MODE_PRIVATE)
                .getString("username", "");
    }

    // ✅ Add Contact (Bound to active user)
    public boolean addToContacts(String fname, String lname, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FNAME, fname);
        values.put(COLUMN_LNAME, lname);
        values.put(COLUMN_CONTACT, contact);
        values.put(COLUMN_USERNAME, getLoggedInUsername());
        long result = db.insert(TABLE_NAME, null, values);
        db.close();
        return result != -1;
    }

    // ✅ Update Contact (Bound to active user)
    public boolean updateContact(int id, String fname, String lname, String contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FNAME, fname);
        values.put(COLUMN_LNAME, lname);
        values.put(COLUMN_CONTACT, contact);

        int rows = db.update(TABLE_NAME, values, 
                COLUMN_ID + "=? AND " + COLUMN_USERNAME + "=?", 
                new String[]{String.valueOf(id), getLoggedInUsername()});
        db.close();
        return rows > 0;
    }

    // ✅ Delete Contact (Bound to active user)
    public boolean deleteContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, 
                COLUMN_ID + "=? AND " + COLUMN_USERNAME + "=?", 
                new String[]{String.valueOf(id), getLoggedInUsername()});
        db.close();
        return rows > 0;
    }

    // ✅ Get Active User's Contacts Only
    public Cursor getAllContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USERNAME + " = ?", 
                new String[]{getLoggedInUsername()});
    }

    // ✅ Get Single Contact by ID (Bound to active user)
    public Cursor getContactById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ? AND " + COLUMN_USERNAME + " = ?", 
                new String[]{String.valueOf(id), getLoggedInUsername()});
    }
}
