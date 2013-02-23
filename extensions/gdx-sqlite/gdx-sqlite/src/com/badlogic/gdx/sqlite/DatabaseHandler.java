package com.badlogic.gdx.sqlite;



public interface DatabaseHandler {	
	
	public void setupDatabase();	
	public void openOrCreateDatabase();
	public void closeDatabae();
	
	public void execSQL(String sql);
	public DatabaseCursor rawQuery(String sql);
	
}
