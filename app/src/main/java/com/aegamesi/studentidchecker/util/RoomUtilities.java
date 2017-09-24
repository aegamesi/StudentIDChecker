package com.aegamesi.studentidchecker.util;

import com.aegamesi.studentidchecker.models.Room;
import com.aegamesi.studentidchecker.models.RoomAssignment;
import com.aegamesi.studentidchecker.models.RoomInfo;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.realm.Realm;
import okio.BufferedSink;
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

	public static void saveRoomInfoToJSON(Realm realm, OutputStream os) throws IOException {
		Moshi moshi = new Moshi.Builder()
				.add(new RealmListJsonAdapter())
				.build();

		RoomInfo roomInfo = new RoomInfo();
		roomInfo.rooms = realm.where(Room.class).findAll();
		roomInfo.assignments = realm.where(RoomAssignment.class).findAll();

		BufferedSink sink = Okio.buffer(Okio.sink(os));
		moshi.adapter(RoomInfo.class).toJson(sink, roomInfo);
		sink.flush();
	}
}
