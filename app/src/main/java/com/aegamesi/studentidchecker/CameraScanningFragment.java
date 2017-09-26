package com.aegamesi.studentidchecker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.aegamesi.studentidchecker.util.AndroidUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class CameraScanningFragment extends ScanningFragment implements ZBarScannerView.ResultHandler {
	private ZBarScannerView scannerView;
	private static final int PERMISSION_REQUEST_CAMERA = 4;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scanning_camera, container, false);

		scannerView = new ZBarScannerView(getActivity());
		scannerView.setFormats(Collections.singletonList(BarcodeFormat.CODE39));

		FrameLayout cameraFrame = (FrameLayout) v.findViewById(R.id.camera_frame);
		cameraFrame.addView(scannerView);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		scannerView.stopCamera();
	}

	@Override
	public void onResume() {
		super.onResume();
		startCamera();
	}

	@SuppressLint("NewApi")
	private void startCamera() {
		if (AndroidUtil.haveCameraPermission(getActivity())) {
			scannerView.setResultHandler(this);
			scannerView.startCamera();
		} else {
			requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (permissions.length == 0 || grantResults.length == 0) {
			return;
		}

		if (requestCode == PERMISSION_REQUEST_CAMERA) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startCamera();
			} else {
				getActivity().finish();
			}
		}
	}

	@Override
	public void handleResult(Result result) {
		barcodeScanned(result.getContents());
	}

	@Override
	public void notifyBarcodeHandled() {
		scannerView.resumeCameraPreview(this);
	}
}
