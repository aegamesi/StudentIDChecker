package com.aegamesi.studentidchecker.util;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.RoomAssignment;
import com.aegamesi.studentidchecker.models.RoomAssignmentCondition;
import com.aegamesi.studentidchecker.models.RoomInfo;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import okio.BufferedSource;
import okio.Okio;

public class RoomUtilities {
	public static void loadRoomInfoFromJSON(Realm realm, InputStream is) throws IOException {
		Moshi moshi = new Moshi.Builder()
				.add(new RealmListJsonAdapter())
				.build();
		BufferedSource source = Okio.buffer(Okio.source(is));
		RoomInfo roomInfo = moshi.adapter(RoomInfo.class).fromJson(source);

		if (roomInfo != null) {
			realm.executeTransaction((r) -> {
				r.delete(Room.class);
				r.delete(RoomAssignment.class);

				r.copyToRealm(roomInfo.rooms);
				r.copyToRealm(roomInfo.assignments);
			});
		}
	}
}
