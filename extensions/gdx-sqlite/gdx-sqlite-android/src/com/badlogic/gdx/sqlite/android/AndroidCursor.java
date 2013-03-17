
package com.badlogic.gdx.sqlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.sql.DatabaseFactory;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** @author M Rafay Aleem */
public class AndroidCursor implements DatabaseCursor {

	private Cursor cursor = null;

	@Override
	public byte[] getBlob (int columnIndex) {
		try {
			return cursor.getBlob(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the blob", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public double getDouble (int columnIndex) {
		try {
			return cursor.getDouble(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the double", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public float getFloat (int columnIndex) {
		try {
			return cursor.getFloat(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the float", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public int getInt (int columnIndex) {
		try {
			return cursor.getInt(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the int", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public long getLong (int columnIndex) {
		try {
			return cursor.getLong(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the long", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public short getShort (int columnIndex) {
		try {
			return cursor.getShort(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the short", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public String getString (int columnIndex) {
		try {
			return cursor.getString(columnIndex);
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in getting the string", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public boolean next () {
		try {
			return cursor.moveToNext();
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in moving the cursor to next", e);
			throw new GdxRuntimeException(e);
		}
	}

	@Override
	public int getCount () {
		return cursor.getCount();
	}

	@Override
	public void close () {
		try {
			cursor.close();
		} catch (SQLiteException e) {
			Gdx.app.log(DatabaseFactory.ERROR_TAG, "There was an error in closing the cursor", e);
			throw new GdxRuntimeException(e);
		}
	}

	public void setNativeCursor (Cursor cursorRef) {
		cursor = cursorRef;
	}
}
