package aurelienribon.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Collection of utility methods to process text in various ways.
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class TextUtils {
	/**
	 * Trims every string of a collection.
	 */
	public static void trim(List<String> strs) {
		for (int i=0, n=strs.size(); i<n; i++) strs.set(i, strs.get(i).trim());
	}

	/**
	 * Trims every string of an array.
	 */
	public static void trim(String[] strs) {
		for (int i=0, n=strs.length; i<n; i++) strs[i] = strs[i].trim();
	}

	/**
	 * Splits the lines of a string, and trims each line.
	 */
	public static List<String> splitAndTrim(String str) {
		String[] strs = str.split("\n");
		List<String> list = Arrays.asList(strs);
		trim(list);
		for (int i=list.size()-1; i>=0; i--) if (list.get(i).isEmpty()) list.remove(i);
		return list;
	}
}
