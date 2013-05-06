
package com.badlogic.gdx.sqlite.desktop;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.DatabaseManager;
import com.badlogic.gdx.sql.SQLiteGdxException;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author M Rafay Aleem */
public class DesktopDatabaseManager implements DatabaseManager {

	private class DesktopDatabase implements Database {

		private SQLiteDatabaseHelper helper = null;

		private final String dbName;
		private final int dbVersion;
		private final String dbOnCreateQuery;
		private final String dbOnUpgradeQuery;

		private Connection connection = null;
		private Statement stmt = null;

		private DesktopDatabase (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
			this.dbName = dbName;
			this.dbVersion = dbVersion;
			this.dbOnCreateQuery = dbOnCreateQuery;
			this.dbOnUpgradeQuery = dbOnUpgradeQuery;
		}

		@Override
		public void setupDatabase () {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				Gdx.app.log(DatabaseFactory.ERROR_TAG,
					"Unable to load the SQLite JDBC driver. There might be a problem with your build path or project setup.", e);
				throw new GdxRuntimeException(e);
			}
		}

		@Override
		public void openOrCreateDatabase () throws SQLiteGdxException {
			if (helper == null) helper = new SQLiteDatabaseHelper(dbName, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);

			try {
				connection = DriverManager.getConnection("jdbc:sqlite:" + dbName);
				stmt = connection.createStatement();
				helper.onCreate(stmt);
			} catch (SQLException e) {
				throw new SQLiteGdxException(e);
			}
		}

		@Override
		public void closeDatabase () throws SQLiteGdxException {
			try {
				stmt.close();
				connection.close();
			} catch (SQLException e) {
				throw new SQLiteGdxException(e);
			}
		}

		@Override
		public void execSQL (String sql) throws SQLiteGdxException {
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e) {
				throw new SQLiteGdxException(e);
			}
		}

		@Override
		public DatabaseCursor rawQuery (String sql) throws SQLiteGdxException {
			DesktopCursor lCursor = new DesktopCursor();
			try {
				ResultSet resultSetRef = stmt.executeQuery(sql);
				lCursor.setNativeCursor(resultSetRef);
				return lCursor;
			} catch (SQLException e) {
				throw new SQLiteGdxException(e);
			}
		}

		@Override
		public DatabaseCursor rawQuery (DatabaseCursor cursor, String sql) throws SQLiteGdxException {
			DesktopCursor lCursor = (DesktopCursor)cursor;
			try {
				ResultSet resultSetRef = stmt.executeQuery(sql);
				lCursor.setNativeCursor(resultSetRef);
				return lCursor;
			} catch (SQLException e) {
				throw new SQLiteGdxException(e);
			}
		}

	}

	@Override
	public Database getNewDatabase (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		return new DesktopDatabase(dbName, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
	}

}
