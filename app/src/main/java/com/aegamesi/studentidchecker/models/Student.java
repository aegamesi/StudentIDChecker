package com.aegamesi.studentidchecker.models;

import io.realm.RealmObject;

public class Student extends RealmObject {
	private String name;
	private long studentId;
	private long userId;
	private String email;
	private int sectionDis;
	private int sectionLab;
	private int sectionLec;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStudentId() {
		return studentId;
	}

	public void setStudentId(long studentId) {
		this.studentId = studentId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getSectionDis() {
		return sectionDis;
	}

	public void setSectionDis(int sectionDis) {
		this.sectionDis = sectionDis;
	}

	public int getSectionLab() {
		return sectionLab;
	}

	public void setSectionLab(int sectionLab) {
		this.sectionLab = sectionLab;
	}

	public int getSectionLec() {
		return sectionLec;
	}

	public void setSectionLec(int sectionLec) {
		this.sectionLec = sectionLec;
	}
}