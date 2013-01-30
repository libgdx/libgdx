package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.badlogic.gdx.DatabaseHandler;
import com.badlogic.gdx.backends.android.database.SQLiteDatabaseHelper;

public class AndroidDatabaseHandler implements DatabaseHandler {

	private SQLiteDatabaseHelper helper;
	private SQLiteDatabase database;
	private Context context;
	
	
	public AndroidDatabaseHandler(Context context) {
		this.context = context;
	}
	
	@Override
	public void setupDatabase (String databaseName, int databaseVersion, String databaseCreateQuery) {
			helper = new SQLiteDatabaseHelper(this.context, databaseName, null, databaseVersion, databaseCreateQuery);			
	}
	
	@Override
	public void openOrCreateDatabase() {
		database = helper.getWritableDatabase();
	}
	
	@Override
	public void execSQL(String sql) {
		database.execSQL(sql);
	}

	@Override
	public void closeDatabae () {
		database.close();		
	}

}
