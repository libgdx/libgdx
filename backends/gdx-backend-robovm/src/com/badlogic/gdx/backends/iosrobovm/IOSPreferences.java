package com.badlogic.gdx.backends.iosrobovm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.robovm.cocoatouch.foundation.NSMutableDictionary;
import org.robovm.cocoatouch.foundation.NSNumber;
import org.robovm.cocoatouch.foundation.NSObject;
import org.robovm.cocoatouch.foundation.NSString;
import org.robovm.objc.ObjCClass;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class IOSPreferences implements Preferences {

	static {
		// FIXME: Work around for a bug in RoboVM (https://github.com/robovm/robovm/issues/155).
		//        These calls make sure NSNumber and NSString have been registered properly with the
		//        RoboVM Objective-C bridge. Without them the get-methods below may throw ClassCastException.
		ObjCClass.getByType(NSNumber.class);
		ObjCClass.getByType(NSString.class);
	}
	
	NSMutableDictionary<NSString, NSObject> nsDictionary;
	String filePath;

	public IOSPreferences (NSMutableDictionary<NSString, NSObject> nsDictionary, String filePath) {
		this.nsDictionary = nsDictionary;
		this.filePath = filePath;
	}

	@Override
	public void putBoolean (String key, boolean val) {
		nsDictionary.put(convertKey(key), NSNumber.valueOf(val));
	}

	@Override
	public void putInteger (String key, int val) {
		nsDictionary.put(convertKey(key), NSNumber.valueOf(val));
	}

	@Override
	public void putLong (String key, long val) {
		nsDictionary.put(convertKey(key), NSNumber.valueOf(val));
	}

	@Override
	public void putFloat (String key, float val) {
		nsDictionary.put(convertKey(key), NSNumber.valueOf(val));
	}

	@Override
	public void putString (String key, String val) {
		nsDictionary.put(convertKey(key), new NSString(val));
	}

	@Override
	public void put (Map<String, ?> vals) {
		Set<String> keySet = vals.keySet();
		for (String key : keySet) {
			Object value = vals.get(key);
			if (value instanceof String) {
				putString(key, (String)value);
			} else if (value instanceof Boolean) {
				putBoolean(key, (Boolean)value);
			} else if (value instanceof Integer) {
				putInteger(key, (Integer)value);
			} else if (value instanceof Long) {
				putLong(key, (Long)value);
			} else if (value instanceof Float) {
				putFloat(key, (Float)value);
			}
		}
	}

	@Override
	public boolean getBoolean (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(convertKey(key));
		if (value == null) return false;
		return value.booleanValue();
	}

	@Override
	public int getInteger (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(convertKey(key));
		if (value == null) return 0;
		return value.intValue();
	}

	@Override
	public long getLong (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(convertKey(key));
		if (value == null) return 0L;
		return value.longValue();
	}

	@Override
	public float getFloat (String key) {
		NSNumber value = (NSNumber)nsDictionary.get(convertKey(key));
		if (value == null) return 0f;
		return value.floatValue();
	}

	@Override
	public String getString (String key) {
		NSString value = (NSString)nsDictionary.get(convertKey(key));
		if (value == null) return "";
		return value.toString();
	}

	@Override
	public boolean getBoolean (String key, boolean defValue) {
		if (!contains(key)) return defValue;
		return getBoolean(key);
	}

	@Override
	public int getInteger (String key, int defValue) {
		if (!contains(key)) return defValue;
		return getInteger(key);
	}

	@Override
	public long getLong (String key, long defValue) {
		if (!contains(key)) return defValue;
		return getLong(key);
	}

	@Override
	public float getFloat (String key, float defValue) {
		if (!contains(key)) return defValue;
		return getFloat(key);
	}

	@Override
	public String getString (String key, String defValue) {
		if (!contains(key)) return defValue;
		return getString(key);
	}

	@Override
	public Map<String, ?> get () {
		 Map<String, Object> map = new HashMap<String, Object>();
		 for (NSString key : nsDictionary.keySet()) {
			 NSObject value = nsDictionary.get(key);
			 map.put(key.toString(), value.toString());
		 }
		 return map;
	}

	@Override
	public boolean contains (String key) {
		return nsDictionary.containsKey(convertKey(key));
	}

	@Override
	public void clear () {
		nsDictionary.clear();
	}

	@Override
	public void remove (String key) {
		nsDictionary.remove(convertKey(key));
	}

	private NSString convertKey (String key) {
		return new NSString(key);
	}

	@Override
	public void flush () {
		boolean fileWritten = nsDictionary.writeToFile(filePath, false);
		if (fileWritten)
			Gdx.app.debug("IOSPreferences", "NSDictionary file written");
		else
			Gdx.app.debug("IOSPreferences", "Failed to write NSDictionary to file " + filePath);
	}

}