package com.example.remindersapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

public class RemindersDbAdapter {

    //these are the column names
    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "content";
    public static final String COL_IMPORTANT = "important";
    //these are the corresponding indices
    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID + 1;
    public static final int INDEX_IMPORTANT = INDEX_ID + 2;
    //used for logging
    private static final String TAG = "RemindersDbAdapter";
    private static final String DATABASE_NAME = "dba_remdrs";
    private static final String TABLE_NAME = "tbl_remdrs";
    private static final int DATABASE_VERSION = 1;
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDbRD;
    private SQLiteDatabase mDbWR;

    //SQL statement used to create the database
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_CONTENT + " TEXT, " +
                    COL_IMPORTANT + " INTEGER );";


    public RemindersDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    //open
    public void open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDbWR = mDbHelper.getWritableDatabase();
        mDbRD = mDbHelper.getReadableDatabase();
    }

    //close
    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }


    //TODO implement the function createReminder() which take the name as the content of the reminder and boolean important...note that the id will be created for you automatically
    public void createReminder(String content, int important) {
//        this.open();
        ContentValues values = new ContentValues();
        values.put(this.COL_CONTENT, content);
        values.put(this.COL_IMPORTANT, important);
        mDbWR.insert(this.TABLE_NAME, null, values);
//        this.close();
    }

    //TODO overloaded to take a reminder
    public long createReminder(@NotNull Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(this.COL_CONTENT, reminder.getContent());
        values.put(this.COL_IMPORTANT, reminder.getImportant());
        long id = mDbWR.insert(this.TABLE_NAME, null, values);
        return id;
    }

    //TODO implement the function fetchReminderById() to get a certain reminder given its id
    public Reminder fetchReminderById(int id) {
        Cursor result = mDbRD.query(TABLE_NAME, new String[]{COL_ID,
                        COL_CONTENT, COL_IMPORTANT}, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null
        );
        if(result != null)
            result.moveToFirst();

        int reminderId = result.getInt(INDEX_ID);
        int important = result.getInt(INDEX_IMPORTANT);
        String content = result.getString(INDEX_CONTENT);
        Reminder reminder = new Reminder(reminderId, content, important);
        return reminder;
    }


    //TODO implement the function fetchAllReminders() which get all reminders
    public Cursor fetchAllReminders() {
        Cursor result = mDbRD.query(TABLE_NAME, new String[]{COL_ID,
                        COL_CONTENT, COL_IMPORTANT},
                null, null, null, null, null
        );

        if(result != null)
            result.moveToFirst();

        return result;
    }

    //TODO implement the function updateReminder() to update a certain reminder
    public void updateReminder(@NotNull Reminder reminder) {
        ContentValues values = new ContentValues();
        values.put(this.COL_CONTENT, reminder.getContent());
        values.put(this.COL_IMPORTANT, reminder.getImportant());
        mDbWR.update(this.TABLE_NAME, values, this.COL_ID + "=?", new String[] {Integer.toString(reminder.getId())});
    }

    //TODO implement the function deleteReminderById() to delete a certain reminder given its id
    public void deleteReminderById(int nId) {
        mDbWR.delete(this.TABLE_NAME, this.COL_ID + "=?", new String[] {Integer.toString(nId)});
    }

    //TODO implement the function deleteAllReminders() to delete all reminders
    public void deleteAllReminders()
    {
        mDbWR.delete(this.TABLE_NAME, null, null);
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(@NotNull SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(@NotNull SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }


}
