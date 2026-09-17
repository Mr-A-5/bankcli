package com.bankcli.persistence;

import com.bankcli.domain.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogDAOImpl implements LogDAO {

	private final String fileName;

	public LogDAOImpl(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public List<Log> getAllLogs() {
		List<Log> logs = new ArrayList<>();

		File file = new File(fileName);
		if (!file.exists()) {
			return logs;
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;

			while ((line = reader.readLine()) != null) {
				if (!line.trim().isEmpty()) {
					logs.add(Log.fromFileToString(line));
				}
			}
		} catch (IOException e) {
			System.out.println("Error reading file: " + e.getMessage());
		}

		return logs;
	}

	@Override
	public void writeLog(Log log) {
		List<Log> oldLogs = new ArrayList<>();
		oldLogs = getAllLogs();
		oldLogs.addFirst(log);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
			for (Log l : oldLogs) {
				writer.write(l.toFileString());
				writer.newLine();
			}
		} catch (IOException e) {
			System.out.println("Error writing file: " + e.getMessage());
		}
	}
}
