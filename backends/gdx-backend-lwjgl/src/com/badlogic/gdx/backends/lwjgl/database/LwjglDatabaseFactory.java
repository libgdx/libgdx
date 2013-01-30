package com.badlogic.gdx.backends.lwjgl.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.badlogic.gdx.DatabaseHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.database.DatabaseFactory;
import com.badlogic.gdx.database.GdxCursor;

public class LwjglDatabaseFactory implements DatabaseFactory {
	
	private class LwjglDatabaseHandler implements DatabaseHandler {
		
		private SQLiteDatabaseHelper helper = null;
		
		private final String DATABASE_NAME;
		private final int DATABASE_VERSION;
		private final String DATABASE_ONCREATE_QUERY;
		private final String DATABASE_ONUPGRADE_QUERY;
		
		private Connection connection = null;
		private Statement stmt = null;
		
		public LwjglDatabaseHandler(String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
			this.DATABASE_NAME = dbName;
			this.DATABASE_VERSION = dbVersion;
			this.DATABASE_ONCREATE_QUERY = dbOnCreateQuery;
			this.DATABASE_ONUPGRADE_QUERY = dbOnUpgradeQuery;
		}

		@Override
		public void setupDatabase () {			
			try {
				Class.forName("org.sqlite.JDBC");				
			} catch (ClassNotFoundException e) {
				Gdx.app.log("Database", "Unable to load the SQLite JDBC driver. Please see your build path.", e);
			}
		}

		@Override
		public void openOrCreateDatabase () {			
			if(helper == null) helper = new SQLiteDatabaseHelper(DATABASE_NAME, DATABASE_VERSION, DATABASE_ONCREATE_QUERY, DATABASE_ONUPGRADE_QUERY);
			
			try {
				connection = DriverManager.getConnection("jdbc:sqlite:" + DATABASE_NAME);
				stmt = connection.createStatement();
				helper.onCreate(stmt);
			} catch(SQLException e) {
				Gdx.app.log("Database", "There was an error in opening: " + DATABASE_NAME, e);
			}			
		}

		@Override
		public void closeDatabae () {
			try {
				stmt.close();
				connection.close();
			} catch (SQLException e) {
				Gdx.app.log("Database", "There was an error in closing the database: " + DATABASE_NAME, e);
			}						
		}

		@Override
		public void execSQL (String sql) {
			try {
				stmt.executeUpdate(sql);
			} catch (SQLException e) {
				Gdx.app.log("Database", "There was an error in executing the queery.", e);
			}
		}

		@Override
		public GdxCursor rawQuery (String sql) {
			LwjglCursor lCursor = new LwjglCursor();
			try {
				lCursor.resultSet = stmt.executeQuery(sql);
				return lCursor;
			} catch (SQLException e) {
				Gdx.app.log("Database", "There was an error in executing the query.", e);
			}
			return null;
		}
		
	}

//	Factory Methods -----	
	
	@Override
	public DatabaseHandler getNewDatabaseHandler (String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		return new LwjglDatabaseHandler(dbName, dbVersion, dbOnCreateQuery, dbOnUpgradeQuery);
	}

}
