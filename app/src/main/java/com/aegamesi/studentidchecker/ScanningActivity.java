package com.aegamesi.studentidchecker;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.ScanResult;
import com.aegamesi.studentidchecker.models.Student;

import java.util.Date;

import io.realm.Realm;

public class ScanningActivity extends AppCompatActivity implements ScanningFragment.OnBarcodeScanListener, View.OnClickListener {
	private ScanningFragment scanner;
	private Realm realm;
	private SharedPreferences prefs;

	private ScanResult lastScan = null;

	private View viewScanResult;
	private View viewScanButtons;
	private TextView textScanName;
	private TextView textScanStatus;
	private Button buttonConfirm;
	private Button buttonCancel;
	private ImageView imageScanImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanning);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		realm = Realm.getDefaultInstance();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		viewScanResult = findViewById(R.id.scan_result);
		viewScanButtons = findViewById(R.id.scan_buttons);
		textScanName = (TextView) findViewById(R.id.scan_name);
		textScanStatus = (TextView) findViewById(R.id.scan_status);
		buttonCancel = (Button) findViewById(R.id.cancel);
		buttonConfirm = (Button) findViewById(R.id.confirm);
		imageScanImage = (ImageView) findViewById(R.id.scan_image);

		buttonConfirm.setOnClickListener(this);
		buttonCancel.setOnClickListener(this);
		viewScanResult.setVisibility(View.GONE);

		// set up scanning fragment
		switch (getIntent().getStringExtra("scanner")) {
			default: // default: physical
			case "physical":
				scanner = new PhysicalScanningFragment();
				break;
			case "camera":
				scanner = new CameraScanningFragment();
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
	public void onClick(View view) {
		if (view == buttonConfirm || view == buttonCancel) {
			if (view == buttonCancel) {
				viewScanResult.setVisibility(View.GONE);
			}

			if (view == buttonConfirm) {
				realm.executeTransaction((r) -> {
					r.copyToRealm(lastScan);
				});
				Log.i("Scanner", "total scan results in realm: " + realm.where(ScanResult.class).count());
				viewScanButtons.setVisibility(View.GONE);
			}

			imageScanImage.setVisibility(View.GONE);
			scanner.notifyBarcodeHandled();
			lastScan = null;
		}
	}

	@Override
	public void onBarcodeScanned(String barcode) {
		ScanResult result = generateScanResult(barcode);
		lastScan = result;

		viewScanResult.setVisibility(View.VISIBLE);
		viewScanButtons.setVisibility(View.VISIBLE);
		textScanName.setText(result.getNameMessage(this));
		textScanStatus.setText(result.getStatusMessage(this, realm));

		// TODO find and load image into imageScanImage
		if (result.student != null && result.student.photo != null) {
			byte[] data = result.student.photo;
			imageScanImage.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
			imageScanImage.setVisibility(View.VISIBLE);
		}

		Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		vibrator.vibrate(result.status == ScanResult.STATUS_OK ? 100 : 1500);
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

		Room assignedRoom = result.student.getAssignedRoom(realm);
		if (assignedRoom != null) {
			String assignedRoomId = assignedRoom.id;
			boolean isCorrect = result.scannerRoom.equals(assignedRoomId) || result.scannerRoom.equals(Room.OTHER_ID);
			if (!isCorrect) {
				result.status = ScanResult.STATUS_BAD_ROOM;
				return result;
			}
		}

		result.status = ScanResult.STATUS_OK;
		return result;
	}
}
