package com.badlogic.gdx.backends.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.badlogic.gdx.DatabaseHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.database.DatabaseFactory;
import com.badlogic.gdx.database.GdxCursor;

public class AndroidDatabaseFactory implements DatabaseFactory {
	
	private Context context;
	
	private class AndroidDatabaseHandler implements DatabaseHandler {

		private SQLiteDatabaseHelper helper;
		private SQLiteDatabase database;
		private Context context;
		
		private final String DATABASE_NAME;
		private final int DATABASE_VERSION;
		private final String DATABASE_ONCREATE_QUERY;
		private final String DATABASE_ONUPGRADE_QUERY;
		
		public AndroidDatabaseHandler(Context context, String dbName, int dbVersion, String dbOnCreateQuery, String dbOnUpgradeQuery) {
			this.context = context;
			this.DATABASE_NAME = dbName;
			this.DATABASE_VERSION = dbVersion;
			this.DATABASE_ONCREATE_QUERY = dbOnCreateQuery;
			this.DATABASE_ONUPGRADE_QUERY = dbOnUpgradeQuery;
		}
		
		@Override
		public void setupDatabase () {
				helper = new SQLiteDatabaseHelper(this.context, DATABASE_NAME, null, DATABASE_VERSION, DATABASE_ONCREATE_QUERY, DATABASE_ONUPGRADE_QUERY);			
		}
		
		@Override
		public void openOrCreateDatabase() {
			database = helper.getWritableDatabase();
		}

		@Override
		public void closeDatabae () {
			helper.close();		
		}
		
		@Override
		public void execSQL(String sql) {
			database.execSQL(sql);
		}
		
		@Override
		public GdxCursor rawQuery(String sql) {
			AndroidCursor aCursor = new AndroidCursor();
			aCursor.cursor = database.rawQuery(sql, null);
			return aCursor;
		}
		
	}
	
//	Factory methods -----	
	
	public AndroidDatabaseFactory(Context context) {
		this.context = context;
	}
	
	@Override
	public DatabaseHandler getNewDatabaseHandler (String datbaseName, int databaseVersion, String databaseCreateQuery, String dbOnUpgradeQuery) {
		return new AndroidDatabaseHandler(this.context, datbaseName, databaseVersion, databaseCreateQuery, dbOnUpgradeQuery);
	}

}
