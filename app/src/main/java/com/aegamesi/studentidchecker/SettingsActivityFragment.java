package com.aegamesi.studentidchecker;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.Student;
import com.aegamesi.studentidchecker.util.RoomUtilities;
import com.aegamesi.studentidchecker.util.StudentUtilities;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final int REQUEST_LOAD_ROSTER = 1;
	private static final int REQUEST_LOAD_ROOMINFO = 2;

	private Preference prefLoadRoster;
	private Preference prefLoadRoominfo;
	private ListPreference prefCurrentRoom;
	private EditTextPreference prefScannerName;

	private Realm realm;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		realm = Realm.getDefaultInstance();

		setupPreferences();
		updateUI();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		realm.close();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOAD_ROSTER) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				try {
					InputStream is = getActivity().getContentResolver().openInputStream(uri);
					StudentUtilities.loadRosterFromCSV(realm, is);
					updateUI();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
				}
			}
		}

		if (requestCode == REQUEST_LOAD_ROOMINFO) {
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				try {
					InputStream is = getActivity().getContentResolver().openInputStream(uri);
					RoomUtilities.loadRoomInfoFromJSON(realm, is);
					// prefCurrentRoom.setValueIndex(0);
					updateUI();
				} catch (IOException e) {
					e.printStackTrace();
					Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	private void setupPreferences() {
		prefLoadRoster = findPreference("roster_load");
		prefLoadRoominfo = findPreference("roominfo_load");
		prefCurrentRoom = (ListPreference) findPreference("current_room");
		prefScannerName = (EditTextPreference) findPreference("scanner_name");

		prefLoadRoster.setOnPreferenceClickListener(preference -> {
			// load CSV file
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			Intent i = Intent.createChooser(intent, getString(R.string.roster_select_csv));
			startActivityForResult(i, REQUEST_LOAD_ROSTER);
			return true;
		});
		prefLoadRoominfo.setOnPreferenceClickListener(preference -> {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			Intent i = Intent.createChooser(intent, getString(R.string.roominfo_select_json));
			startActivityForResult(i, REQUEST_LOAD_ROOMINFO);
			return true;
		});

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		prefs.registerOnSharedPreferenceChangeListener(this);

		prefCurrentRoom.setOnPreferenceChangeListener((preference, o) -> {
			updateUI();
			return true;
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		Preference pref = findPreference(s);

		if (pref instanceof ListPreference) {
			ListPreference listPreference = (ListPreference) pref;
			listPreference.setSummary(listPreference.getEntry());
		} else if (pref instanceof EditTextPreference) {
			EditTextPreference editTextPreference = (EditTextPreference) pref;
			editTextPreference.setSummary(editTextPreference.getText());
		}
	}

	private void updateUI() {
		int numStudents = (int) realm.where(Student.class).count();
		prefLoadRoster.setSummary(String.format(getString(R.string.roster_num_students), numStudents));
		int numRooms = (int) realm.where(Room.class).count();
		prefLoadRoominfo.setSummary(String.format(getString(R.string.roominfo_num_rooms), numRooms));

		String[] roomNames = new String[numRooms];
		String[] roomIds = new String[numRooms];
		RealmResults<Room> rooms = realm.where(Room.class).findAll();
		for (int i = 0; i < rooms.size(); i++) {
			roomNames[i] = rooms.get(i).name;
			roomIds[i] = rooms.get(i).id;
		}
		prefCurrentRoom.setEntries(roomNames);
		prefCurrentRoom.setEntryValues(roomIds);
		prefCurrentRoom.setSummary(prefCurrentRoom.getEntry());
		prefScannerName.setSummary(prefScannerName.getText());
	}
}