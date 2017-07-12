package com.aegamesi.studentidchecker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.ScanResult;
import com.aegamesi.studentidchecker.models.Student;

import java.util.Date;

import io.realm.Realm;

public class ScanningActivity extends AppCompatActivity implements ScanningFragment.OnBarcodeScanListener {
	private ScanningFragment scanner;
	private Realm realm;
	private SharedPreferences prefs;

	private View viewScanResult;
	private TextView textScanName;
	private TextView textScanStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanning);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		realm = Realm.getDefaultInstance();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		viewScanResult = findViewById(R.id.scan_result);
		textScanName = (TextView) findViewById(R.id.scan_name);
		textScanStatus = (TextView) findViewById(R.id.scan_status);

		viewScanResult.setVisibility(View.GONE);

		// set up scanning fragment
		switch (getIntent().getStringExtra("scanner")) {
			default: // default: physical
			case "physical":
				scanner = new PhysicalScanningFragment();
				break;
			case "camera":
				// TODO
				break;
		}
		getFragmentManager().beginTransaction().add(R.id.scanning_fragment, scanner).commit();
		scanner.setOnBarcodeScanListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		realm.close();
	}

	@Override
	public void onBarcodeScanned(String barcode) {
		ScanResult result = generateScanResult(barcode);

		viewScanResult.setVisibility(View.VISIBLE);
		textScanName.setText(result.getNameMessage(this));
		textScanStatus.setText(result.getStatusMessage(this, realm));
	}

	private ScanResult generateScanResult(String barcode) {
		ScanResult result = new ScanResult();
		result.scannedAt = new Date();
		result.scannerName = prefs.getString("scanner_name", "[unknown name]");
		result.scannerRoom = prefs.getString("current_room", "[unknown room]");
		result.barcode = barcode;

		result.student = realm.where(Student.class).equalTo("barcode", barcode).findFirst();
		if (result.student == null) {
			result.status = ScanResult.STATUS_NOT_ENROLLED;
			return result;
		}

		String correctRoom = result.student.getAssignedRoom(realm).id;
		boolean isCorrectRoom = result.scannerRoom.equals(correctRoom) || result.scannerRoom.equals(Room.OTHER_ID);
		if (!isCorrectRoom) {
			result.status = ScanResult.STATUS_BAD_ROOM;
			return result;
		}

		result.status = ScanResult.STATUS_OK;
		return result;
	}
}
