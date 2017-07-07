package com.aegamesi.studentidchecker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.Student;
import com.aegamesi.studentidchecker.util.RoomUtilities;
import com.aegamesi.studentidchecker.util.StudentUtilities;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

public class SettingsActivityFragment extends PreferenceFragment {
	private static final int REQUEST_LOAD_ROSTER = 1;
	private static final int REQUEST_LOAD_ROOMINFO = 2;

	private Preference prefLoadRoster;
	private Preference prefLoadRoominfo;

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
			boolean success = false;
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				try {
					InputStream is = getActivity().getContentResolver().openInputStream(uri);
					StudentUtilities.loadRosterFromCSV(realm, is);
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (success) {
				updateUI();
			} else {
				Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
			}
		}

		if (requestCode == REQUEST_LOAD_ROOMINFO) {
			boolean success = false;
			if (resultCode == Activity.RESULT_OK) {
				Uri uri = data.getData();

				try {
					InputStream is = getActivity().getContentResolver().openInputStream(uri);
					RoomUtilities.loadRoomInfoFromJSON(realm, is);
					success = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (success) {
				updateUI();
			} else {
				Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void setupPreferences() {
		prefLoadRoster = findPreference("roster_load");
		prefLoadRoominfo = findPreference("roominfo_load");

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
	}

	private void updateUI() {
		long numStudents = realm.where(Student.class).count();
		prefLoadRoster.setSummary(String.format(getString(R.string.roster_num_students), numStudents));
		long numRooms = realm.where(Room.class).count();
		prefLoadRoominfo.setSummary(String.format(getString(R.string.roominfo_num_rooms), numRooms));
	}
}