package com.aegamesi.studentidchecker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.aegamesi.studentidchecker.models.ScanResult;
import com.aegamesi.studentidchecker.util.AndroidUtil;
import com.aegamesi.studentidchecker.util.LogUtilities;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MenuActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		findViewById(R.id.scan_physical).setOnClickListener((view) -> {
			Intent intent = new Intent(this, ScanningActivity.class);
			intent.putExtra("scanner", "physical");
			startActivity(intent);
		});
		findViewById(R.id.scan_camera).setOnClickListener((view) -> {
			Intent intent = new Intent(this, ScanningActivity.class);
			intent.putExtra("scanner", "camera");
			startActivity(intent);
		});
		findViewById(R.id.export_log).setOnClickListener((view) -> {
			Realm realm = Realm.getDefaultInstance();
			RealmResults<ScanResult> results = realm.where(ScanResult.class).findAllSorted("scannedAt", Sort.ASCENDING);
			File csv = LogUtilities.exportLogToCSV(this, results);
			realm.close();

			if (csv != null) {
				Uri contentUri = FileProvider.getUriForFile(this, "com.aegamesi.studentidchecker.fileprovider", csv);
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
				sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				sendIntent.setType("text/csv");
				startActivity(Intent.createChooser(sendIntent, getString(R.string.log_export)));
			} else {
				Toast.makeText(this, R.string.error, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_menu, menu);

		MenuItem itemSettings = menu.findItem(R.id.action_settings);
		AndroidUtil.setMenuItemIcon(this, itemSettings, R.drawable.settings);
		itemSettings.setOnMenuItemClickListener((item) -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		});

		return true;
	}

}
