# gdx-sqlite

gdx-sqlite is a cross-platform Libgdx extension for SQLite database handling. The extension abstracts databse handling to provide a unified method to handle database transacitons across multiple platforms while also adding SQLite support for desktop version of Libgdx application.

Currently supported platforms:
- Android (Implemented using Android SQLite API)
- Desktop (SQLite JDBC from https://bitbucket.org/xerial/sqlite-jdbc/wiki/Home)

A small portion of code has been adapted from the tutorial located at:  http://www.vogella.com/articles/AndroidSQLite/article.html 

Latest build of this extension can be downloaded from: http://bit.ly/gdx-sqlite

## Extension setup in a Libgdx application:

Note: This setup assumes that you have properly setup your project as follows (or similar to the following):
 - App
 - AppDesktop
 - AppAndroid

#### For App project:
 - Copy gdx-sqlite.jar into your App project libs folder
 - In the Libraries tab of your Java Build Path, add the gdx-sqlite.jar
 - In the Order and Export tab of your Java Build Path, make sure that gdx-sqlite.jar is checked

#### For AppDesktop project:
 - Copy gdx-sqlite-desktop.jar and sqlite-jdbc-3.7.2.jar into your AppDesktop project libs folder
 - In the Libraries tab of your Java Build Path, add the gdx-sqlite-desktop.jar and sqlite-jdbc-3.7.2.jar

#### For AppAndroid project:
 - Copy gdx-sqlite-android.jar into your AppAndroid project libs folder
 - In the Libraries tab of your Java Build Path, add the gdx-sqlite-android.jar
 - In the Order and Export tab of your Java Build Path, make sure that gdx-sqlite-android.jar is checked

## Example Code:
```java
package com.mrafayaleem.gdxsqlitetest;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.Database;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.sql.SQLiteGdxException;

public class DatabaseTest {

	Database dbHandler;

	public static final String TABLE_COMMENTS = "comments";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_COMMENT = "comment";

	private static final String DATABASE_NAME = "comments.db";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table if not exists "
			+ TABLE_COMMENTS + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_COMMENT
			+ " text not null);";

	public DatabaseTest() {
		Gdx.app.log("DatabaseTest", "creation started");
		dbHandler = DatabaseFactory.getNewDatabase(DATABASE_NAME,
				DATABASE_VERSION, DATABASE_CREATE, null);

		dbHandler.setupDatabase();
		try {
			dbHandler.openOrCreateDatabase();
			dbHandler.execSQL(DATABASE_CREATE);
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		Gdx.app.log("DatabaseTest", "created successfully");

		try {
			dbHandler
					.execSQL("INSERT INTO comments ('comment') VALUES ('This is a test comment')");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}

		DatabaseCursor cursor = null;

		try {
			cursor = dbHandler.rawQuery("SELECT * FROM comments");
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		while (cursor.next()) {
			Gdx.app.log("FromDb", String.valueOf(cursor.getString(1)));
		}

		try {
			dbHandler.closeDatabase();
		} catch (SQLiteGdxException e) {
			e.printStackTrace();
		}
		dbHandler = null;
		Gdx.app.log("DatabaseTest", "dispose");
	}
}
```
## License:
This extension follows the Apache License version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)

See License FAQ http://www.apache.org/foundation/licence-FAQ.html for more details.

## Reporting Bugs:
Please email any bugs or feature requests at: mrafayaleem at gmail.com

## Author:
M. Rafay Aleem