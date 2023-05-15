package net.app;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.sqlite.SQLiteConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class DataBase {

	private File file;

	private Connection conn;
	private Statement st;

	public DataBase(File file) {
		this.file = file;

		ensureFileExistence();
		try {
			SQLiteConfig config = new SQLiteConfig();
			config.enforceForeignKeys(true);
			conn = DriverManager.getConnection("jdbc:sqlite:" + file, config.toProperties());
			st = conn.createStatement();
			st.setQueryTimeout(30);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		loadTables();
	}

	public DataBase() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://82.123.52.171:25565/expensemanager", "testuser",
					"password");
			st = conn.createStatement();
			st.setQueryTimeout(30);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		loadTables();
	}

	private void ensureFileExistence() {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadTables() {
		String indexData = Utils.loadFile("tables/tables.json");
		JsonObject index = JsonParser.parseString(indexData).getAsJsonObject();
		JsonArray tableArray = index.get("tables").getAsJsonArray();
		Iterator<JsonElement> it = tableArray.iterator();
		while (it.hasNext()) {
			String tableName = it.next().getAsString();
			String tableCreator = Utils.loadFile("tables/" + tableName + ".sqltable");
			try {
				st.executeUpdate(tableCreator);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Connection getConnection() {
		return conn;
	}

	public Statement getStatement() {
		return st;
	}

}