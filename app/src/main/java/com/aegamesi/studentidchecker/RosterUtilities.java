package com.aegamesi.studentidchecker;

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

public class RosterUtilities {
	public static void loadRosterFromCSV(InputStream is) throws IOException {
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

		Map<String, Object> studentMap;
		while ((studentMap = csv.read(header, processors)) != null) {
			System.out.println(studentMap.get("Name") + " -- " + studentMap.get("Student ID"));
		}
	}
}
