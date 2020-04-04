package com.example.remindersapp;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

/**
 * Created by engMa_000 on 2017-04-03.
 */

public class RemindersSimpleCursorAdapter extends CursorAdapter {

    public RemindersSimpleCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.reminder,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView contentView = (TextView) view.findViewById(R.id.reminder_text);
        int contentIndex = cursor.getColumnIndex(RemindersDbAdapter.COL_CONTENT);
        contentView.setText(cursor.getString(contentIndex));

        int importantIndex = cursor.getColumnIndexOrThrow(RemindersDbAdapter.COL_IMPORTANT);
        if (cursor.getInt(importantIndex) > 0) {
            view.setBackgroundColor(ContextCompat.getColor(context,R.color.orange));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
        }
    }
}