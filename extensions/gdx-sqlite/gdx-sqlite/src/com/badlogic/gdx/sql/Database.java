
package com.badlogic.gdx.sql;

/** This public interface contains the necessary methods to setup and execute queries on a database. The factory method
 * {@link DatabaseFactory#getNewDatabase(String, int, String, String)} will return a database object that implements this
 * interface. The typical sequence of method calls should be as follows:
 * <ul>
 * <li>{@link Database#setupDatabase()}</li>
 * <li>{@link Database#openOrCreateDatabase()}</li>
 * <li>{@link Database#execSQL(String)} OR</li>
 * <li>{@link Database#rawQuery(String)} OR</li>
 * <li>{@link Database#rawQuery(DatabaseCursor, String)}</li>
 * <li>{@link Database#closeDatabase()}</li>
 * </ul>
 * @author M Rafay Aleem */
public interface Database {

	/** This method is needed to be called only once before any database related activity can be performed. The method performs the
	 * necessary procedures for the database. However, a database will not be opened/created until
	 * {@link Database#openOrCreateDatabase()} is called. */
	public void setupDatabase ();

	/** Opens an already existing database or creates a new database if it doesn't already exists. */
	public void openOrCreateDatabase ();

	/** Closes the opened database and releases all the resources related to this database. */
	public void closeDatabase ();

	/** Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
	 * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported. */
	public void execSQL (String sql);

	/** Runs the provided SQL and returns a {@link DatabaseCursor} over the result set.
	 * @param sql the SQL query. The SQL string must not be ; terminated
	 * @return {@link DatabaseCursor} */
	public DatabaseCursor rawQuery (String sql);

	/** Runs the provided SQL and returns the same {@link DatabaseCursor} that was passed to this method. Use this method when you
	 * want to avoid reallocation of {@link DatabaseCursor} object. Note that you shall only pass the {@link DatabaseCursor} object
	 * that was previously returned by a rawQuery method. Creating your own {@link DatabaseCursor} and then passing it as an object
	 * will not work.
	 * @param cursor existing {@link DatabaseCursor} object
	 * @param sql the SQL query. The SQL string must not be ; terminated
	 * @return the passed {@link DatabaseCursor}. */
	public DatabaseCursor rawQuery (DatabaseCursor cursor, String sql);

}
