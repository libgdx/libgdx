
package com.badlogic.gdx.sql;

/** Result codes taken from http://www.sqlite.org/c3ref/c_abort.html
 * 
 * @author M Rafay Aleem */
public class SQLiteResultCodes {

	public final static int SQLITE_OK = 0; /* Successful result */
	public final static int SQLITE_ERROR = 1; /* SQL error or missing database */
	public final static int SQLITE_INTERNAL = 2; /* Internal logic error in SQLite */
	public final static int SQLITE_PERM = 3; /* Access permission denied */
	public final static int SQLITE_ABORT = 4; /* Callback routine requested an abort */
	public final static int SQLITE_BUSY = 5; /* The database file is locked */
	public final static int SQLITE_LOCKED = 6; /* A table in the database is locked */
	public final static int SQLITE_NOMEM = 7; /* A malloc() failed */
	public final static int SQLITE_READONLY = 8; /* Attempt to write a readonly database */
	public final static int SQLITE_INTERRUPT = 9; /* Operation terminated by sqlite3_interrupt() */
	public final static int SQLITE_IOERR = 10; /* Some kind of disk I/O error occurred */
	public final static int SQLITE_CORRUPT = 11; /* The database disk image is malformed */
	public final static int SQLITE_NOTFOUND = 12; /* Unknown opcode in sqlite3_file_control() */
	public final static int SQLITE_FULL = 13; /* Insertion failed because database is full */
	public final static int SQLITE_CANTOPEN = 14; /* Unable to open the database file */
	public final static int SQLITE_PROTOCOL = 15; /* Database lock protocol error */
	public final static int SQLITE_EMPTY = 16; /* Database is empty */
	public final static int SQLITE_SCHEMA = 17; /* The database schema changed */
	public final static int SQLITE_TOOBIG = 18; /* String or BLOB exceeds size limit */
	public final static int SQLITE_CONSTRAINT = 19; /* Abort due to constraint violation */
	public final static int SQLITE_MISMATCH = 20; /* Data type mismatch */
	public final static int SQLITE_MISUSE = 21; /* Library used incorrectly */
	public final static int SQLITE_NOLFS = 22; /* Uses OS features not supported on host */
	public final static int SQLITE_AUTH = 23; /* Authorization denied */
	public final static int SQLITE_FORMAT = 24; /* Auxiliary database format error */
	public final static int SQLITE_RANGE = 25; /* 2nd parameter to sqlite3_bind out of range */
	public final static int SQLITE_NOTADB = 26; /* File opened that is not a database file */
	public final static int SQLITE_ROW = 100; /* sqlite3_step() has another row ready */
	public final static int SQLITE_DONE = 101; /* sqlite3_step() has finished executing */

}
