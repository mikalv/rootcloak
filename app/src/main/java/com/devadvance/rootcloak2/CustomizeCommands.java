package com.devadvance.rootcloak2;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomizeCommands extends PreferenceActivity {

    SharedPreferences sharedPref;
    Set<String> commandSet;
    String[] commandList;
    boolean isFirstRunCommands;

    @SuppressLint("WorldReadableFiles")
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_commands);
        // Show the Up button in the action bar.
        setupActionBar();

        sharedPref = Common.COMMANDS.getSharedPreferences(this);

        loadList();

        if (sharedPref.getBoolean(Common.SHOW_WARNING, true)) {
            Resources res = getResources();
            new AlertDialog.Builder(this)
                    .setMessage(res.getString(R.string.command_instructions) + "\n\n" + res.getString(R.string.both_instructions2))
                    .setTitle(res.getString(R.string.important_title))
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            sharedPref.edit().putBoolean(Common.SHOW_WARNING, false).apply();
                        }
                    })
                    .show();
        }

    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        final int positionFinal = position;
        new AlertDialog.Builder(CustomizeCommands.this)
                .setTitle(R.string.remove_command_title)
                .setMessage(R.string.remove_command_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        removeCommand(positionFinal);
                        loadList();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();

    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    private void setupActionBar() {

        ActionBar ab = getActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customize_commands, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                final EditText input = new EditText(this);
                new AlertDialog.Builder(CustomizeCommands.this)
                        .setTitle(R.string.add_command)
                        .setMessage(R.string.input_command)
                        .setView(input)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                savePref(input.getText().toString());
                                loadList();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
                return true;
            case R.id.action_load_defaults:
                loadDefaultsWithConfirm();
                return true;
            case R.id.action_clear_list:
                clearList();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDefaults() {
        commandSet = Common.COMMANDS.getDefaultSet();
        Editor editor = sharedPref.edit();
        editor.remove(Common.COMMANDS.getSetKey());
        editor.apply();
        editor.putStringSet(Common.COMMANDS.getSetKey(), commandSet);
        editor.apply();
        editor.putBoolean(Common.FIRST_RUN_KEY, false);
        editor.apply();
        loadList();
    }

    private void loadDefaultsWithConfirm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeCommands.this)
                .setTitle(R.string.reset)
                .setMessage(getString(R.string.reset_keywords))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loadDefaults();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing on cancel
            }
        }).show();
    }

    private void loadList() {
        commandSet = sharedPref.getStringSet(Common.COMMANDS.getSetKey(), new HashSet<String>());
        isFirstRunCommands = sharedPref.getBoolean(Common.FIRST_RUN_KEY, true);
        if (isFirstRunCommands) {
            if (commandSet.isEmpty()) {
                loadDefaults();
            } else {
                Editor editor = sharedPref.edit();
                editor.putBoolean(Common.FIRST_RUN_KEY, false);
                editor.apply();
            }
        }
        commandList = commandSet.toArray(new String[commandSet.size()]);
        Arrays.sort(commandList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, commandList);
        // Bind to our new adapter.
        setListAdapter(adapter);
    }

    private void clearList() {
        final Editor editor = sharedPref.edit();
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeCommands.this)
                .setTitle(R.string.clear)
                .setMessage(R.string.clear_all_commands)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        editor.remove(Common.COMMANDS.getSetKey());
                        editor.apply();
                        loadList();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing on cancel
            }
        }).show();
    }

    private void savePref(String command) {
        if (!(commandSet.contains(command))) {
            commandSet.add(command);
            Editor editor = sharedPref.edit();
            editor.remove(Common.COMMANDS.getSetKey());
            editor.apply();
            editor.putStringSet(Common.COMMANDS.getSetKey(), commandSet);
            editor.apply();
            editor.putBoolean(Common.FIRST_RUN_KEY, false);
            editor.apply();
        }
    }

    private void removeCommand(int position) {
        String tempName = commandList[position];
        commandSet.remove(tempName);
        Editor editor = sharedPref.edit();
        editor.remove(Common.COMMANDS.getSetKey());
        editor.apply();
        editor.putStringSet(Common.COMMANDS.getSetKey(), commandSet);
        editor.apply();
    }

}
