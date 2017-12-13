/*
 * Code taken from FaceSlim by indywidualny. Thanks.
 */
package me.zeeroooo.materialfb.Fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import me.zeeroooo.materialfb.Misc.BlackListH;
import me.zeeroooo.materialfb.Misc.BlacklistAdapter;
import me.zeeroooo.materialfb.Misc.DatabaseHelper;
import me.zeeroooo.materialfb.R;

public class NotificationsSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;
    private DatabaseHelper DBHelper;
    private BlackListH blh;
    private BlacklistAdapter adapter;
    private ArrayList<BlackListH> blacklist;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.notifications_settings);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        DBHelper = new DatabaseHelper(getActivity());
        blacklist = new ArrayList<>();
        final Cursor data = DBHelper.getListContents();
        while (data.moveToNext()) {
            if (data.getString(3) != null) {
                blh = new BlackListH(data.getString(3));
                blacklist.add(blh);
            }
        }
        Preference BlackList = findPreference("BlackList");
        BlackList.setOnPreferenceClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the listener
        preferences.registerOnSharedPreferenceChangeListener(prefChangeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the listener
        preferences.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
        DBHelper.close();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            case "BlackList":
                AlertDialog.Builder BlacklistDialog = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View view = inflater.inflate(R.layout.blacklist, null);
                final EditText blword = view.findViewById(R.id.blword_new);
                BlacklistDialog.setView(view);
                BlacklistDialog.setTitle(R.string.blacklist_title);
                adapter = new BlacklistAdapter(getActivity(), blacklist, DBHelper);
                ListView BlackListView = view.findViewById(R.id.BlackListView);
                BlackListView.setAdapter(adapter);

                BlacklistDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String word = blword.getText().toString();
                        if (!word.equals("")) {
                            blh = new BlackListH(word);
                            DBHelper.addData(null, null, blh.getWord());
                            blacklist.add(blh);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });

                BlacklistDialog.setCancelable(false);
                BlacklistDialog.show();
                return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update notification ringtone preference summary
        String ringtoneString = preferences.getString("ringtone", "content://settings/system/notification_sound");
        Uri ringtoneUri = Uri.parse(ringtoneString);
        String name;

        try {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            name = ringtone.getTitle(getActivity());
        } catch (Exception ex) {
            ex.printStackTrace();
            name = "Default";
        }

        if ("".equals(ringtoneString))
            name = getString(R.string.silent);

        RingtonePreference rpn = (RingtonePreference) findPreference("ringtone");
        rpn.setSummary(getString(R.string.notification_sound_description) + name);

        // update message ringtone preference summary
        ringtoneString = preferences.getString("ringtone_msg", "content://settings/system/notification_sound");
        ringtoneUri = Uri.parse(ringtoneString);

        try {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
            name = ringtone.getTitle(getActivity());
        } catch (Exception ex) {
            ex.printStackTrace();
            name = "Default";
        }

        if ("".equals(ringtoneString))
            name = getString(R.string.silent);

        RingtonePreference rpm = (RingtonePreference) findPreference("ringtone_msg");
        rpm.setSummary(getString(R.string.notification_sound_description) + name);
    }
}