package com.aegamesi.studentidchecker.util;

import android.content.Context;

import com.aegamesi.studentidchecker.models.ScanResult;

import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseLong;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.realm.Realm;

public class ExportUtilities {
	private static File getExportFile(Context context, String filename) {
		File exportPath = new File(context.getFilesDir(), "exports");
		exportPath.mkdirs();
		return new File(exportPath, filename);
	}

	public static File exportLogToCSV(Context context, Iterable<ScanResult> scans) {
		File csvFile = getExportFile(context, "scans.csv");

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));
			CsvMapWriter csv = new CsvMapWriter(writer, CsvPreference.STANDARD_PREFERENCE);

			final CellProcessor[] processors = new CellProcessor[]{
					new FmtDate("yyyy-MM-dd hh:mm:ss a"), // Scanned At
					new NotNull(), // Scanner Name
					new NotNull(), // Scanner Room,
					new NotNull(), // Barcode
					new Optional(), // Name
					new NotNull(), // Status
			};
			String[] headers = {"Scanned At", "Scanner Name", "Scanner Room", "Barcode", "Name", "Status"};
			csv.writeHeader(headers);

			Map<String, Object> map = new HashMap<>();
			for (ScanResult result : scans) {
				map.clear();
				map.put(headers[0], result.scannedAt);
				map.put(headers[1], result.scannerName);
				map.put(headers[2], result.scannerRoom);
				map.put(headers[3], result.barcode);
				map.put(headers[4], result.student == null ? null : result.student.name);
				map.put(headers[5], result.getStatusName());
				csv.write(map, headers, processors);
			}

			csv.close();
			return csvFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File exportSettingsToZIP(Context context, Realm realm) {
		File zipFile = getExportFile(context, "settings.zip");
		ZipEntry ze;

		try {
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

			zos.putNextEntry(new ZipEntry("rooms.json"));
			RoomUtilities.saveRoomInfoToJSON(realm, zos);
			zos.closeEntry();

			zos.setLevel(Deflater.NO_COMPRESSION);
			zos.putNextEntry(new ZipEntry("photos.zip"));
			StudentUtilities.savePhotosToZIP(realm, zos);
			zos.closeEntry();
			zos.setLevel(Deflater.DEFAULT_COMPRESSION);

			zos.putNextEntry(new ZipEntry("roster.csv"));
			StudentUtilities.saveRosterToCSV(realm, zos);
			zos.closeEntry();

			zos.close();
			return zipFile;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
