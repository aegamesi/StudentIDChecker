package com.aegamesi.studentidchecker.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RoomAssignment extends RealmObject {
	public String room;
	public RealmList<RoomAssignmentCondition> conditions;

	public boolean matches(Student student) {
		for (RoomAssignmentCondition condition : conditions) {
			if (!condition.evaluate(student)) {
				return false;
			}
		}

		return true;
	}
}
