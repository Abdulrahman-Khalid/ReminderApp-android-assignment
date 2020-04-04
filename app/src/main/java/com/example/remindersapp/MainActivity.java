package com.example.remindersapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;



public class MainActivity extends AppCompatActivity {

    private ListView remindersList;
    private RemindersDbAdapter dbAdapter;
    private RemindersSimpleCursorAdapter cursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.ic_notifications);

        //create RemindersDbAdapter instance
        dbAdapter = new RemindersDbAdapter(this);
        dbAdapter.open();

        // get reminder list view
        remindersList =(ListView) findViewById(R.id.reminders_list);
        remindersList.setDivider(null);
        // fetch all reminders
        Cursor c = dbAdapter.fetchAllReminders();
        // create RemindersSimpleCursorAdapter and set ListView adaptor to it
        cursorAdapter = new RemindersSimpleCursorAdapter(MainActivity.this, c);
        remindersList.setAdapter(cursorAdapter);
        // if item in the list is clicked
        remindersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int reminderPosition, long id) {
                showOptionsDialog(reminderPosition);
            }
        });

        remindersList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        remindersList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) { }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.delete_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete_selected:
                        for (int i = cursorAdapter.getCount() - 1; i >= 0; i--) {
                            if (remindersList.isItemChecked(i)) {
                                dbAdapter.deleteReminderById((int)cursorAdapter.getItemId(i));
                            }
                        }
                        mode.finish();
                        cursorAdapter.changeCursor(dbAdapter.fetchAllReminders());
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) { //open new reminder dialog
            case R.id.newReminderOprtion:
                showRemiderDialog(false,null);
                return true;
            case R.id.exitOption: //exit application
                finish();
                System.exit(0);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // options dialog
    private void showOptionsDialog(final int reminderListPosition) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int reminderId = (int)cursorAdapter.getItemId(reminderListPosition);
                if (which == 0) { // edit button clicked
                    Reminder reminder = dbAdapter.fetchReminderById(reminderId);
                    showRemiderDialog(true,reminder);
                    //delete reminder
                } else { // delete button clicked
                    dbAdapter.deleteReminderById(reminderId);
                    cursorAdapter.changeCursor(dbAdapter.fetchAllReminders());
                }
            }
        });
        builder.show();
    }

    private void showRemiderDialog(final boolean shouldUpdate, final Reminder r) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.reminder_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);
        alertDialogBuilderUserInput.setTitle(shouldUpdate ? R.string.edit_reminder_title : R.string.add_reminder_title);

        // views
        final EditText inputReminder = view.findViewById(R.id.reminder_text);
        final CheckBox importantCheckBox = (CheckBox) view.findViewById(R.id.important_checkbox);
        final Button commitBtn = (Button) view.findViewById(R.id.commit_button);
        // set checkBox and editText field if it's update not add
        if (shouldUpdate && r != null) {
            inputReminder.setText(r.getContent());
            boolean isImportant = (r.getImportant()>0)? true:false;
            importantCheckBox.setChecked(isImportant);
        }

        alertDialogBuilderUserInput.setCancelable(false);
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        commitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputReminder.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter Reminder!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (shouldUpdate && r != null) {
                    Reminder newReminder = new Reminder(r.getId(),inputReminder.getText().toString(),(importantCheckBox.isChecked())?1:0);
                    dbAdapter.updateReminder(newReminder);
                } else {
                    dbAdapter.createReminder(inputReminder.getText().toString(),(importantCheckBox.isChecked())?1:0);
                }
                //TODO refresh
                Cursor c = dbAdapter.fetchAllReminders();
                cursorAdapter.changeCursor(c);
                alertDialog.dismiss();
            }
        });

        final Button cancelBtn = (Button) view.findViewById(R.id.cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbAdapter.close();
    }
}
