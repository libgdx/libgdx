
package com.badlogic.gdx.sqlite.android;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.DatabaseManager;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author M Rafay Aleem */
public class AndroidDatabaseManager implements DatabaseManager {

	private Context context;

	private class AndroidDatabase implements Database {

		private SQLiteDatabaseHelper helper;
		private SQLiteDatabase database;
		private Context context;

		private final String dbName;
		private final int dbVersion;
		private final String dbOnCreateQuery;
		private final String dbOnUpgradeQuery;

		private AndroidDatabase (Context context, String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
			this.context = context;
			this.dbName = dbName;
			this.dbVersion = dbVersion;
			this.dbOnCreateQuery = dbOnCreateQuery;
			this.dbOnUpgradeQuery = dbOnUpgradeQuery;
		}

		@Override
		public void setupDatabase () {
			helper = new SQLiteDatabaseHelper(this.context, dbName, null, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
		}

		@Override
		public void openOrCreateDatabase () {
			try {
				database = helper.getWritableDatabase();
			} catch (SQLiteException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG, "Cannot get a writable database.", e);
				throw new GdxRuntimeException(e);
			}
		}

		@Override
		public void closeDatabase () {
			try {
				helper.close();
			} catch (SQLiteException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG, "Cannot close the database.", e);
			}
		}

		@Override
		public void execSQL (String sql) {
			try {
				database.execSQL(sql);
			} catch (SQLException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG, "Cannot execute the query on database.", e);
			}
		}

		@Override
		public DatabaseCursor rawQuery (String sql) {
			AndroidCursor aCursor = new AndroidCursor();
			try {
				Cursor tmp = database.rawQuery(sql, null);
				aCursor.setNativeCursor(tmp);
				return aCursor;
			} catch (SQLiteException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG, "Cannot execute the raw query on database.", e);
			}
			return null;
		}

		@Override
		public DatabaseCursor rawQuery (DatabaseCursor cursor, String sql) {
			AndroidCursor aCursor = (AndroidCursor)cursor;
			try {
				Cursor tmp = database.rawQuery(sql, null);
				aCursor.setNativeCursor(tmp);
				return aCursor;
			} catch (SQLiteException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG, "Cannot execute the raw query on database.", e);
			}
			return null;
		}

	}

	public AndroidDatabaseManager () {
		AndroidApplication app = (AndroidApplication)Gdx.app;
		context = app.getApplicationContext();
	}

	@Override
	public Database getNewDatabase (String databaseName, int databaseVersion, String databaseCreateQuery, String dbOnUpgradeQuery) {
		return new AndroidDatabase(this.context, databaseName, databaseVersion, databaseCreateQuery, dbOnUpgradeQuery);
	}

}
