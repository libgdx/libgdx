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

 
package com.dozingcatsoftware.bouncy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONUtils {

	/** If argument is a JSONArray or JSONObject, returns the equivalent List or Map. If argument is JSONObject.NULL, returns null.
	 * Otherwise, returns the argument unchanged. */
	public static Object objectFromJSONItem (Object jsonItem) {
		if (jsonItem == JSONObject.NULL) {
			return null;
		}
		if (jsonItem instanceof JSONArray) {
			return listFromJSONArray((JSONArray)jsonItem);
		}
		if (jsonItem instanceof JSONObject) {
			return mapFromJSONObject((JSONObject)jsonItem);
		}
		return jsonItem;
	}

	/** Returns a List with the same objects in the same order as jsonArray. Recursively converts nested JSONArray and JSONObject
	 * values to List and Map objects. */
	public static List listFromJSONArray (JSONArray jsonArray) {
		List result = new ArrayList();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				Object obj = objectFromJSONItem(jsonArray.get(i));
				result.add(obj);
			}
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	/** Returns a List with the same keys and values as jsonObject. Recursively converts nested JSONArray and JSONObject values to
	 * List and Map objects. */
	public static Map mapFromJSONObject (JSONObject jsonObject) {
		Map result = new HashMap();
		try {
			for (Iterator ki = jsonObject.keys(); ki.hasNext();) {
				String key = (String)ki.next();
				Object value = objectFromJSONItem(jsonObject.get(key));
				result.put(key, value);
			}
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	/** Returns a List created by parsing the string argument as a JSON array and calling listFromJSONArray. */
	public static List listFromJSONString (String jsonString) {
		try {
			return listFromJSONArray(new JSONArray(jsonString));
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** Returns a Map created by parsing the string argument as a JSON object and calling mapFromJSONObject. */
	public static Map mapFromJSONString (String jsonString) {
		try {
			return mapFromJSONObject(new JSONObject(jsonString));
		} catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

}
