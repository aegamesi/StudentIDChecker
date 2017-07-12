package com.aegamesi.studentidchecker.models;

import android.content.Context;

import com.aegamesi.studentidchecker.R;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;

public class ScanResult extends RealmObject {
	public static final int STATUS_OK = 0;
	public static final int STATUS_UNKNOWN_ERROR = 1;
	public static final int STATUS_BAD_ROOM = 2;
	public static final int STATUS_NOT_ENROLLED = 3;
	private static final String[] STATUS_NAMES = {"OK", "UNKNOWN_ERROR", "BAD_ROOM", "NOT_ENROLLED"};

	public Date scannedAt;
	public String scannerName;
	public String scannerRoom;
	public String barcode;
	public Student student;
	public int status;

	public String getNameMessage(Context context) {
		switch (status) {
			case STATUS_OK:
				return student.name;
			case STATUS_BAD_ROOM:
				return student.name;
			case STATUS_NOT_ENROLLED:
				return barcode;
			default:
				return barcode;
		}
	}

	public String getStatusMessage(Context context, Realm realm) {
		switch (status) {
			case STATUS_OK:
				return context.getString(R.string.scanresult_ok);
			case STATUS_BAD_ROOM:
				String correctRoom = student.getAssignedRoom(realm).name;
				return String.format(context.getString(R.string.scanresult_bad_room), correctRoom);
			case STATUS_NOT_ENROLLED:
				return context.getString(R.string.scanresult_not_enrolled);
			default:
				return context.getString(R.string.scanresult_unknown_error);
		}
	}

	public String getStatusName() {
		return STATUS_NAMES[status];
	}
}
