package com.bankcli.domain;

public class Log {

	private String timestamp;
	private String message;
	private String type;

	public Log(String timestamp, String type, String message) {
		this.timestamp = timestamp;
		this.message = message;
		this.type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return String.format("| %-30s | %-7s | %-90s |", getTimestamp().toString(), getType(), getMessage());
	}

	public String toFileString() {
		return String.format("%s,%s,%s", getTimestamp().toString(), getType(), getMessage());
	}

	public static Log fromFileToString(String line) {
		String[] parts = line.split(",");
		return new Log(parts[0], parts[1], parts[2]);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
