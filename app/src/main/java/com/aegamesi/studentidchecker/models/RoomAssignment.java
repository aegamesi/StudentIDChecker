package com.aegamesi.studentidchecker.models;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RoomAssignment extends RealmObject {
	public String room;
	public RealmList<RoomAssignmentCondition> conditions;
}
