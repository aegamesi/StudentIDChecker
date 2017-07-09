package com.aegamesi.studentidchecker;

import android.app.Fragment;

public abstract class ScanningFragment extends Fragment {
	private OnBarcodeScanListener onBarcodeScanListener = null;

	public void setOnBarcodeScanListener(OnBarcodeScanListener listener) {
		onBarcodeScanListener = listener;
	}

	void barcodeScanned(String barcode) {
		if (onBarcodeScanListener != null) {
			onBarcodeScanListener.onBarcodeScanned(barcode);
		}
	}

	public interface OnBarcodeScanListener {
		void onBarcodeScanned(String barcode);
	}
}
