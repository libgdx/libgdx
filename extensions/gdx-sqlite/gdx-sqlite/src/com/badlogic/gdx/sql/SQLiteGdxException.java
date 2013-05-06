
package com.badlogic.gdx.sql;

/** Checked Exception for SQLite used in this extension.
 * 
 * @author M Rafay Aleem */
public class SQLiteGdxException extends Exception {
	private static final long serialVersionUID = 123750592122585758L;

	public SQLiteGdxException (String message) {
		super(message);
	}

	public SQLiteGdxException (Throwable t) {
		super(t);
	}

	public SQLiteGdxException (String message, Throwable t) {
		super(message, t);
	}

}
