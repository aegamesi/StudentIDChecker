package com.aegamesi.studentidchecker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class ScanningActivity extends AppCompatActivity implements ScanningFragment.OnBarcodeScanListener {
	private ScanningFragment scanner;

	private View viewScanResult;
	private TextView textScanName;
	private TextView textScanStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanning);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
	public void onBarcodeScanned(String barcode) {
		viewScanResult.setVisibility(View.VISIBLE);
		textScanStatus.setText(barcode); // XXX temporary
	}
}
