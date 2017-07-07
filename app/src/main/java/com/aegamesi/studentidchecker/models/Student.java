package com.aegamesi.studentidchecker.models;

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
}