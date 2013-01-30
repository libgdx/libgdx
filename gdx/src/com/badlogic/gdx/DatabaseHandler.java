package com.badlogic.gdx;

import com.badlogic.gdx.database.GdxCursor;

public interface DatabaseHandler {	
	
	public void setupDatabase();	
	public void openOrCreateDatabase();
	public void closeDatabae();
	
	public void execSQL(String sql);
	public GdxCursor rawQuery(String sql);
	
}
