package com.aegamesi.studentidchecker.util;

import android.net.Uri;
import android.util.Log;

import com.aegamesi.studentidchecker.models.Student;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;
import io.realm.RealmResults;

public class StudentUtilities {
	private static final Pattern patternDis = Pattern.compile("DIS ([0-9]+)");
	private static final Pattern patternLab = Pattern.compile("LAB ([0-9]+)");
	private static final Pattern patternLec = Pattern.compile("LEC ([0-9]+)");

	private static CellProcessor[] getRosterCSVCellProcessors() {
		return new CellProcessor[]{
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
	}

	public static void loadRosterFromCSV(Realm realm, InputStream is) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(is));
		CsvMapReader csv = new CsvMapReader(reader, CsvPreference.STANDARD_PREFERENCE);

		String[] header = csv.getHeader(true);
		CellProcessor[] processors = getRosterCSVCellProcessors();

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

	public static void saveRosterToCSV(Realm realm, OutputStream os) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(os);
		CsvMapWriter csv = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);

		CellProcessor[] processors = getRosterCSVCellProcessors();
		String[] header = {"Name", "Student ID", "User ID", "Role", "Email Address", "Sections", "Majors", "Terms in Attendance", "Units", "Grading Basis", "Waitlist Position"};
		Map<String, Object> map = new HashMap<>();

		csv.writeHeader(header);
		RealmResults<Student> students = realm.where(Student.class).findAll();
		for (Student student : students) {
			map.clear();
			map.put(header[0], student.name);
			map.put(header[1], student.studentId);
			map.put(header[2], student.userId);
			map.put(header[4], student.email);
			map.put(header[5], String.format(Locale.getDefault(), "LEC %d, DIS %d, LAB %d",
					student.sectionLec, student.sectionDis, student.sectionLab));
			csv.write(map, header, processors);
		}

		csv.flush();
	}

	public static void loadPhotosFromZIP(Realm realm, InputStream is) throws IOException {
		realm.beginTransaction();

		// discard all old photos
		RealmResults<Student> students = realm.where(Student.class).findAll();
		for (Student student : students) {
			student.photo = null;
		}

		byte[] buffer = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ZipInputStream zipInputStream = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				Uri uri = Uri.parse(entry.getName());
				String identifier = uri.getLastPathSegment();

				long userId;
				int extension = identifier.lastIndexOf('.');
				if (extension != -1) {
					identifier = identifier.substring(0, extension);
				}
				try {
					userId = Long.parseLong(identifier);
				} catch (NumberFormatException e) {
					continue;
				}

				int len;
				while ((len = zipInputStream.read(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}

				Student s = realm.where(Student.class).equalTo("userId", userId).findFirst();
				if (s != null) {
					s.photo = baos.toByteArray();
				}

				baos.reset();
			}

			realm.commitTransaction();
		} catch (Exception e) {
			realm.cancelTransaction();
			throw e;
		}
	}

	public static void savePhotosToZIP(Realm realm, OutputStream os) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os);
		zos.setLevel(Deflater.NO_COMPRESSION);

		RealmResults<Student> students = realm.where(Student.class).findAll();
		for (Student student : students) {
			if (student.photo != null) {
				ZipEntry ze = new ZipEntry(student.userId + ".jpg");
				zos.putNextEntry(ze);
				zos.write(student.photo);
				zos.closeEntry();
			}
		}

		zos.finish();
	}
}
