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
import com.aegamesi.studentidchecker.util.AndroidUtil;
import com.aegamesi.studentidchecker.util.ExportUtilities;
import com.aegamesi.studentidchecker.util.RoomUtilities;
import com.aegamesi.studentidchecker.util.StudentUtilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import io.realm.RealmResults;

public class SettingsActivityFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	private static final int REQUEST_LOAD_ROSTER = 1;
	private static final int REQUEST_LOAD_ROOMINFO = 2;
	private static final int REQUEST_LOAD_PHOTOS = 3;

	private Preference prefLoadPhotos;
	private Preference prefLoadRoster;
	private Preference prefLoadRoominfo;
	private Preference prefExportSettings;
	private Preference prefImportSettings;
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
		if (resultCode == Activity.RESULT_OK) {
			receiveFile(data, requestCode, REQUEST_LOAD_ROSTER, (is -> {
				StudentUtilities.loadRosterFromCSV(realm, is);
			}));
			receiveFile(data, requestCode, REQUEST_LOAD_ROOMINFO, is -> {
				RoomUtilities.loadRoomInfoFromJSON(realm, is);
				prefCurrentRoom.setValueIndex(0);
			});
			receiveFile(data, requestCode, REQUEST_LOAD_PHOTOS, is -> {
				StudentUtilities.loadPhotosFromZIP(realm, is);
			});
		}
	}

	private void receiveFile(Intent data, int actual, int expected, IFileLoadHandler handler) {
		if (actual == expected) {
			Uri uri = data.getData();

			try {
				InputStream is = getActivity().getContentResolver().openInputStream(uri);
				handler.handle(is);
				updateUI();
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void requestFile(int requestCode, int prompt) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		Intent i = Intent.createChooser(intent, getString(prompt));
		startActivityForResult(i, requestCode);
	}

	private void setupPreferences() {
		prefLoadPhotos = findPreference("photos_load");
		prefLoadRoster = findPreference("roster_load");
		prefLoadRoominfo = findPreference("roominfo_load");
		prefImportSettings = findPreference("settings_import");
		prefExportSettings = findPreference("settings_export");
		prefCurrentRoom = (ListPreference) findPreference("current_room");
		prefScannerName = (EditTextPreference) findPreference("scanner_name");

		prefLoadRoster.setOnPreferenceClickListener(preference -> {
			requestFile(REQUEST_LOAD_ROSTER, R.string.roster_select_csv);
			return true;
		});
		prefLoadRoominfo.setOnPreferenceClickListener(preference -> {
			requestFile(REQUEST_LOAD_ROOMINFO, R.string.roominfo_select_json);
			return true;
		});
		prefLoadPhotos.setOnPreferenceClickListener(preference -> {
			requestFile(REQUEST_LOAD_PHOTOS, R.string.photos_select_zip);
			return true;
		});

		prefExportSettings.setOnPreferenceClickListener(preference -> {
			File zip = ExportUtilities.exportSettingsToZIP(getActivity(), realm);
			AndroidUtil.shareFile(getActivity(), zip, "application/zip", R.string.settings_export);
			return true;
		});

		prefImportSettings.setOnPreferenceClickListener(preference -> {
			Intent intent = new Intent(getActivity(), ConfigScanningActivity.class);
			startActivity(intent);
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
		int numPhotos = (int) realm.where(Student.class).isNotNull("photo").count();
		prefLoadPhotos.setSummary(String.format(getString(R.string.photos_num_photos), numPhotos));

		String[] roomNames = new String[numRooms + 1];
		String[] roomIds = new String[numRooms + 1];
		roomNames[0] = getString(R.string.other_room);
		roomIds[0] = Room.OTHER_ID;
		RealmResults<Room> rooms = realm.where(Room.class).findAll();
		for (int i = 0; i < rooms.size(); i++) {
			roomNames[i + 1] = rooms.get(i).name;
			roomIds[i + 1] = rooms.get(i).id;
		}
		prefCurrentRoom.setEntries(roomNames);
		prefCurrentRoom.setEntryValues(roomIds);
		prefCurrentRoom.setSummary(prefCurrentRoom.getEntry());
		prefScannerName.setSummary(prefScannerName.getText());
	}

	private interface IFileLoadHandler {
		void handle(InputStream is) throws IOException;
	}
}