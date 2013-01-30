package com.badlogic.gdx.database;

import com.badlogic.gdx.DatabaseHandler;

public interface DatabaseFactory {

	/**
	 * This method will return a handler to an existing or a not-yet-created database. You will need to manually call methods on the handler to setup, open/create or close the database.
	 * See {@link DatabaseHandler} for more details.
	 * @param dbName The name of the database.
	 * @param dbVersion Database version.
	 * @param dbCreateQuery The query that should be executed on the creation of the database. This query would usually create the necessary tables in the database.
	 * @param dbOnUpgradeQuery The query that should be executed on upgrading the database from an old version to a new one.
	 * @return Returns a handler to an existing or not-yet-created database.
	 */
	public DatabaseHandler getNewDatabaseHandler(String dbName, int dbVersion, String dbCreateQuery, String dbOnUpgradeQuery);
	
}
