package com.aegamesi.studentidchecker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MenuActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		findViewById(R.id.button_settings).setOnClickListener((view) -> {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		});
	}
}
