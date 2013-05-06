
package com.badlogic.gdx.sqlite.desktop;

import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.Gdx;

/** @author M Rafay Aleem */
public class SQLiteDatabaseHelper {

	private final String dbName;
	private final int dbVersion;
	private final String dbOnCreateQuery;
	private final String dbOnUpgradeQuery;

	public SQLiteDatabaseHelper (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		this.dbName = dbName;
		this.dbVersion = dbVersion;
		this.dbOnCreateQuery = dbOnCreateQuery;
		this.dbOnUpgradeQuery = dbOnUpgradeQuery;
	}

	public void onCreate (Statement stmt) throws SQLException {
		if (dbOnCreateQuery != null) stmt.executeUpdate(dbOnCreateQuery);
	}

	public void onUpgrade (Statement stmt, int oldVersion, int newVersion) throws SQLException {
		if (dbOnUpgradeQuery != null) {
			stmt.executeUpdate(dbOnUpgradeQuery);
			onCreate(stmt);
		}
	}

}
