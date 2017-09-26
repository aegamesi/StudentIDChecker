package com.aegamesi.studentidchecker;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class PhysicalScanningFragment extends ScanningFragment {
	private EditText textBarcode;
	private Button buttonGo;

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.scanning_physical, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		textBarcode = (EditText) view.findViewById(R.id.barcode_input);
		buttonGo = (Button) view.findViewById(R.id.barcode_go);

		buttonGo.setOnClickListener((v) -> {
			submitBarcode();
		});
		textBarcode.setOnKeyListener((v, keyCode, keyEvent) -> {
			if (keyEvent.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
				submitBarcode();
				return true;
			}
			return false;
		});
	}

	private void submitBarcode() {
		String barcode = textBarcode.getText().toString();
		if (barcode.length() > 0) {
			barcodeScanned(barcode);
			textBarcode.setText("");

			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(textBarcode.getWindowToken(), 0);
		}
	}

}
