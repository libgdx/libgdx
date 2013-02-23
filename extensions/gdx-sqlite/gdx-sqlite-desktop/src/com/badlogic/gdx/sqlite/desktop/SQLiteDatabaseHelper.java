package com.badlogic.gdx.sqlite.desktop;

import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.Gdx;

public class SQLiteDatabaseHelper {
	
	private final String DB_NAME; 
	private final int DB_VERSION;
	private final String DB_ONCREATE_QUERY;
	private final String DB_ONUPGRADE_QUERY;
	
	public SQLiteDatabaseHelper(String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		this.DB_NAME = dbName;
		this.DB_VERSION = dbVersion;
		this.DB_ONCREATE_QUERY = dbOnCreateQuery;
		this.DB_ONUPGRADE_QUERY = dbOnUpgradeQuery;
	}
	
	public void onCreate(Statement stmt) throws SQLException {
		if(DB_ONCREATE_QUERY != null)
			stmt.executeUpdate(DB_ONCREATE_QUERY);
	}
	
	public void onUpgrade(Statement stmt, int oldVersion, int newVersion) throws SQLException {
		if(DB_ONUPGRADE_QUERY != null) {
			stmt.executeUpdate(DB_ONUPGRADE_QUERY);
			onCreate(stmt);
		}
	}

}
