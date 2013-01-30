package com.badlogic.gdx.backends.android.database;

import android.database.Cursor;

import com.badlogic.gdx.database.GdxCursor;

public class AndroidCursor implements GdxCursor {
	
	public Cursor cursor = null;

	@Override
	public byte[] getBlob (int columnIndex) {
		return cursor.getBlob(columnIndex);
	}

	@Override
	public double getDouble (int columnIndex) {
			return cursor.getDouble(columnIndex);
	}

	@Override
	public float getFloat (int columnIndex) {
		return cursor.getFloat(columnIndex);
	}

	@Override
	public int getInt (int columnIndex) {
		return cursor.getInt(columnIndex);
	}

	@Override
	public long getLong (int columnIndex) {
		return cursor.getLong(columnIndex);
	}

	@Override
	public short getShort (int columnIndex) {
		return cursor.getShort(columnIndex);
	}

	@Override
	public String getString (int columnIndex) {
		return cursor.getString(columnIndex);
	}

	@Override
	public boolean next () {
		return cursor.moveToNext();
	}

	@Override
	public int getCount () {
		return cursor.getCount();
	}

	@Override
	public void close () {
		cursor.close();		
	}

}
