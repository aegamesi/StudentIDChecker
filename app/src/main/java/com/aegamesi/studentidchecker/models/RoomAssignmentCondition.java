package com.aegamesi.studentidchecker.models;

import io.realm.RealmObject;

public class RoomAssignmentCondition extends RealmObject {
	public String key;
	public String op;
	public long val;
}
