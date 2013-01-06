/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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