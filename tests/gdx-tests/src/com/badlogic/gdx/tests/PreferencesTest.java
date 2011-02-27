package com.badlogic.gdx.tests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.tests.utils.GdxTest;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PreferencesTest extends GdxTest {

	@Override public boolean needsGL20 () {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void create() {
		Preferences prefs = Gdx.app.getPreferences(".test");
		prefs.clear();
		prefs.putBoolean("bool", true);
		prefs.putInteger("int", 1234);
		prefs.putLong("long", Long.MAX_VALUE);
		prefs.putFloat("float", 1.2345f);
		prefs.putString("string", "test!");
		
		if(prefs.getBoolean("bool") != true) throw new GdxRuntimeException("bool failed");
		if(prefs.getInteger("int") != 1234) throw new GdxRuntimeException("int failed");
		if(prefs.getLong("long") != Long.MAX_VALUE) throw new GdxRuntimeException("long failed");
		if(prefs.getFloat("float") != 1.2345f) throw new GdxRuntimeException("float failed");
		if(!prefs.getString("string").equals("test!")) throw new GdxRuntimeException("string failed");							
	}
}
