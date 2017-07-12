package com.aegamesi.studentidchecker.models;

import io.realm.RealmObject;

public class Room extends RealmObject {
	public static final String OTHER_ID = "[other]";

	public String id;
	public String name;
}
