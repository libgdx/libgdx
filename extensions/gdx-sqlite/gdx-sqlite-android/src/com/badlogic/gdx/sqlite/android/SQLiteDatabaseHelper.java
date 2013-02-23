package com.badlogic.gdx.sqlite.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDatabaseHelper extends SQLiteOpenHelper
{
	
	private final String DB_NAME; 
	private final int DB_VERSION;
	private final String DB_ONCREATE_QUERY;
	private final String DB_ONUPGRADE_QUERY;

	public SQLiteDatabaseHelper (Context context, String name, CursorFactory factory, int version, String dbOnCreateQuery, String dbOnUpgradeQuery) {
		super(context, name, factory, version);
		this.DB_NAME = name;
		this.DB_VERSION = version;
		this.DB_ONCREATE_QUERY = dbOnCreateQuery;
		this.DB_ONUPGRADE_QUERY = dbOnUpgradeQuery;
	}

	@Override
	public void onCreate (SQLiteDatabase database) {
		if(DB_ONCREATE_QUERY != null)
			database.execSQL(DB_ONCREATE_QUERY);
		
	}

	@Override
	public void onUpgrade (SQLiteDatabase database, int oldVersion, int newVersion) {		
		if(DB_ONUPGRADE_QUERY != null) {
			database.execSQL(DB_ONUPGRADE_QUERY);
			onCreate(database);
		}
	}

}
