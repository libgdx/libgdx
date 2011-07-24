package com.badlogic.gdx.utils;

import com.google.gwt.corp.compatibility.CompatibilityImpl;

public class NumberUtils {
	static CompatibilityImpl impl = new CompatibilityImpl();
	
	public static int floatToIntBits(float value) {
		return impl.floatToIntBits(value);
	}
	
	public static int floatToRawIntBits(float value) {
		return impl.floatToIntBits(value);
	}
	
	public static float intBitsToFloat(int value) {
		return impl.intBitsToFloat(value);
	}
	
	public static long doubleToLongBits(double value) {
		return 0; // FIXME
	}
	
	public static double longBitsToDouble(long value) {
		return 0; // FIXME
	}
}
