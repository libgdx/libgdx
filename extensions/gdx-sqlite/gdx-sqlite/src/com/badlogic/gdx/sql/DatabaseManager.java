
package com.badlogic.gdx.sql;

/** @author M Rafay Aleem */
public interface DatabaseManager {

	/** This method will return a reference to an existing or a not-yet-created database. You will need to manually call methods on
	 * the {@link Database} object to setup, open/create or close the database. See {@link Database} for more details. <b> Note:
	 * </b> dbOnUpgradeQuery will only work on an Android device. It will be executed when you increment your database version
	 * number. First, dbOnUpgradeQuery will be executed (Where you will generally perform activities such as dropping the tables,
	 * etc.). Then dbOnCreateQuery will be executed. However, dbOnUpgradeQuery won't be executed on downgrading the database
	 * version.
	 * @param dbName The name of the database.
	 * @param dbVersion number of the database (starting at 1); if the database is older, dbOnUpgradeQuery will be used to upgrade
	 *           the database (on Android only)
	 * @param dbOnCreateQuery The query that should be executed on the creation of the database. This query would usually create
	 *           the necessary tables in the database.
	 * @param dbOnUpgradeQuery The query that should be executed on upgrading the database from an old version to a new one.
	 * @return Returns a {@link Database} object pointing to an existing or not-yet-created database. */
	public Database getNewDatabase (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery);
}
