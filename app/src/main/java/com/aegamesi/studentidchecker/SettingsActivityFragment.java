package com.aegamesi.studentidchecker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.aegamesi.studentidchecker.models.Student;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.realm.Realm;

public class SettingsActivityFragment extends PreferenceFragment {
	private static final int REQUEST_LOAD_ROSTER = 1;

	private Preference prefLoadRoster;
	private Preference prefClearRoster;

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
					RosterUtilities.loadRosterFromCSV(realm, is);
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
		prefClearRoster = findPreference("roster_clear");

		prefLoadRoster.setOnPreferenceClickListener(preference -> {
			// load CSV file
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			Intent i = Intent.createChooser(intent, getString(R.string.roster_select_csv));
			startActivityForResult(i, REQUEST_LOAD_ROSTER);
			return true;
		});
		prefClearRoster.setOnPreferenceClickListener(preference -> {
			realm.executeTransaction((r) -> {
				r.delete(Student.class);
			});
			updateUI();
			return true;
		});
	}

	private void updateUI() {
		long numStudents = realm.where(Student.class).count();
		prefLoadRoster.setSummary(String.format(getString(R.string.roster_num_students), numStudents));
	}
}