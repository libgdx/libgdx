
package com.badlogic.gdx.sql;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** A factory class that creates new database objects and returns references to them. See
 * {@link DatabaseFactory#getNewDatabase(String, int, String, String)} for more details.
 * @author M Rafay Aleem */
public class DatabaseFactory {

	public static String ERROR_TAG = "DATABASE";
	private static final String androidClassname = "com.badlogic.gdx.sqlite.android.AndroidDatabaseManager";
	private static final String desktopClassname = "com.badlogic.gdx.sqlite.desktop.DesktopDatabaseManager";

	private static DatabaseManager databaseManager = null;

	/** This is a factory method that will return a reference to an existing or a not-yet-created database. You will need to
	 * manually call methods on the {@link Database} object to setup, open/create or close the database. See {@link Database} for
	 * more details. <b> Note: </b> dbOnUpgradeQuery will only work on an Android device. It will be executed when you increment
	 * your database version number. First, dbOnUpgradeQuery will be executed (Where you will generally perform activities such as
	 * dropping the tables, etc.). Then dbOnCreateQuery will be executed. However, dbOnUpgradeQuery won't be executed on
	 * downgrading the database version.
	 * @param dbName The name of the database.
	 * @param dbVersion number of the database (starting at 1); if the database is older, dbOnUpgradeQuery will be used to upgrade
	 *           the database (on Android only)
	 * @param dbOnCreateQuery The query that should be executed on the creation of the database. This query would usually create
	 *           the necessary tables in the database.
	 * @param dbOnUpgradeQuery The query that should be executed on upgrading the database from an old version to a new one.
	 * @return Returns a {@link Database} object pointing to an existing or not-yet-created database. */
	public static Database getNewDatabase (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		if (databaseManager == null) {
			switch (Gdx.app.getType()) {
			case Android:
				try {
					databaseManager = (DatabaseManager)Class.forName(androidClassname).newInstance();
				} catch (Throwable ex) {
					throw new GdxRuntimeException("Error geting database: " + androidClassname, ex);
				}
				break;
			case Desktop:
				try {
					databaseManager = (DatabaseManager)Class.forName(desktopClassname).newInstance();
				} catch (Throwable ex) {
					throw new GdxRuntimeException("Error geting database: " + desktopClassname, ex);
				}
				break;
			case Applet:
				throw new GdxRuntimeException("SQLite is currently not supported in Applets");
			case WebGL:
				throw new GdxRuntimeException("SQLite is currently not supported in WebGL");
			case iOS:
				throw new GdxRuntimeException("SQLite is currently not supported in iOS");
			}
		}
		return databaseManager.getNewDatabase(dbName, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
	}

	private DatabaseFactory () {
	}

}
