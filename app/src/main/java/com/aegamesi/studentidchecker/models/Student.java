package com.aegamesi.studentidchecker.models;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;

public class Student extends RealmObject {
	public String name;
	@Index
	public String barcode;
	public long studentId;
	public long userId;
	public String email;
	public int sectionDis;
	public int sectionLab;
	public int sectionLec;

	public Room getAssignedRoom(Realm realm) {
		for (RoomAssignment assignment : realm.where(RoomAssignment.class).findAll()) {
			if (assignment.matches(this)) {
				return realm.where(Room.class).equalTo("id", assignment.room).findFirst();
			}
		}

		return null;
	}
}