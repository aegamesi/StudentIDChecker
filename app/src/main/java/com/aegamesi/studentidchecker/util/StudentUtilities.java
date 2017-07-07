package com.aegamesi.studentidchecker.util;

import com.aegamesi.studentidchecker.models.Student;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class StudentUtilities {
	private static final Pattern patternDis = Pattern.compile("DIS ([0-9]{3})");
	private static final Pattern patternLab = Pattern.compile("LAB ([0-9]{3})");
	private static final Pattern patternLec = Pattern.compile("LEC ([0-9]{3})");

	public static void loadRosterFromCSV(Realm realm, InputStream is) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(is));
		CsvMapReader csv = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE);

		final String[] header = csv.getHeader(true);
		final CellProcessor[] processors = new CellProcessor[] {
				new NotNull(), // Name
				new NotNull(new ParseLong()), // Student ID
				new NotNull(new ParseLong()), // User ID
				new Optional(), // Role
				new Optional(), // Email Address
				new NotNull(), // Sections
				new Optional(), // Majors
				new Optional(), // Terms in Attendance
				new Optional(new ParseDouble()), // Units
				new Optional(), // Grading Basis
				new Optional(), // Waitlist Position
		};

		realm.beginTransaction();
		realm.delete(Student.class);
		try {
			Map<String, Object> studentMap;
			while ((studentMap = csv.read(header, processors)) != null) {
				Student student = realm.createObject(Student.class);
				student.name = (String) studentMap.get("Name");
				student.studentId = (long) studentMap.get("Student ID");
				student.userId = (long) studentMap.get("User ID");
				student.email = (String) studentMap.get("Email Address");

				// parse out DIS/LAB/LEC sections
				String sections = (String) studentMap.get("Sections");
				Matcher matcherDis = patternDis.matcher(sections);
				student.sectionDis = matcherDis.find() ? Integer.parseInt(matcherDis.group(1)) : -1;
				Matcher matcherLec = patternLec.matcher(sections);
				student.sectionLec = matcherLec.find() ? Integer.parseInt(matcherLec.group(1)) : -1;
				Matcher matcherLab = patternLab.matcher(sections);
				student.sectionLab = matcherLab.find() ? Integer.parseInt(matcherLab.group(1)) : -1;

				// barcode is last 8 digits of student ID
				// see http://sisproject.berkeley.edu/team/integration/faq
				String barcode = Long.toString(student.studentId);
				if (barcode.length() > 8) {
					barcode = barcode.substring(barcode.length() - 8);
				}
				student.barcode = barcode;
			}

			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
			throw e;
		}
	}
}
