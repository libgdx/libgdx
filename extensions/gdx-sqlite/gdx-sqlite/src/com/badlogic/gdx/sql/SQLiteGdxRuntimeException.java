
package com.badlogic.gdx.sql;

import com.badlogic.gdx.utils.GdxRuntimeException;

/** Unchecked runtime exception for SQLite used in this extension.
 * 
 * @author M Rafay Aleem */
public class SQLiteGdxRuntimeException extends GdxRuntimeException {
	private static final long serialVersionUID = 5859319081184266132L;

	public SQLiteGdxRuntimeException (String message) {
		super(message);
	}

	public SQLiteGdxRuntimeException (Throwable t) {
		super(t);
	}

	public SQLiteGdxRuntimeException (String message, Throwable t) {
		super(message, t);
	}

}
