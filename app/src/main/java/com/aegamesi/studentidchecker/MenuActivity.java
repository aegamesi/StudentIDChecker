package com.aegamesi.studentidchecker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.aegamesi.studentidchecker.util.AndroidUtil;

public class MenuActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
