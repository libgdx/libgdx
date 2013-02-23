package com.badlogic.gdx.sqlite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DatabaseHandlerFactory {
	
	private static final String androidClassname = "com.badlogic.gdx.sqlite.android.AndroidDatabaseManager";
	private static final String desktopClassname = "com.badlogic.gdx.sqlite.desktop.DesktopDatabaseManager";
	
	private static DatabaseManager databaseManager = null;
	
	public static DatabaseHandler getNewDatabaseHandler(String dbName, int dbVersion, String dbCreateQuery, String dbOnUpgradeQuery) {
		if(databaseManager == null) {
			switch(Gdx.app.getType()) {
			case Android:
				try {
					databaseManager = (DatabaseManager)Class.forName(androidClassname).newInstance();
				} catch (Throwable ex) {
					throw new GdxRuntimeException("Error geting database handler: " + androidClassname, ex);
				}
				break;
			case Desktop:
				try {
					databaseManager = (DatabaseManager)Class.forName(desktopClassname).newInstance();
				} catch (Throwable ex) {
					throw new GdxRuntimeException("Error geting database handler: " + desktopClassname, ex);
				}
				break;
			case Applet:
				break;
			case WebGL:
				break;
			case iOS:
				break;
			default:
				break;
			}
		}
		return databaseManager.getNewDatabaseHandler(dbName, dbVersion, dbCreateQuery, dbOnUpgradeQuery);
	}

}
