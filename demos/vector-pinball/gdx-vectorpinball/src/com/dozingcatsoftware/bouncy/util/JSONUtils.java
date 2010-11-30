package com.dozingcatsoftware.bouncy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.json.JSONArray;
import com.badlogic.gdx.utils.json.JSONException;
import com.badlogic.gdx.utils.json.JSONObject;

public class JSONUtils {
	
	/** If argument is a JSONArray or JSONObject, returns the equivalent List or Map. If argument is JSONObject.NULL, returns null.
	 * Otherwise, returns the argument unchanged.
	 */
	public static Object objectFromJSONItem(Object jsonItem) {
		if (jsonItem==JSONObject.NULL){
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

	/** Returns a List with the same objects in the same order as jsonArray. Recursively converts nested JSONArray and JSONObject values
	 * to List and Map objects.
	 */
	public static List listFromJSONArray(JSONArray jsonArray) {
		List result = new ArrayList();
		try {
			for(int i=0; i<jsonArray.length(); i++) {
				Object obj = objectFromJSONItem(jsonArray.get(i));
				result.add(obj);
			}
		}
		catch(JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}
	
	/** Returns a List with the same keys and values as jsonObject. Recursively converts nested JSONArray and JSONObject values
	 * to List and Map objects.
	 */
	public static Map mapFromJSONObject(JSONObject jsonObject) {
		Map result = new HashMap();
		try {
			for(Iterator ki = jsonObject.keys(); ki.hasNext(); ) {
				String key = (String)ki.next();
				Object value = objectFromJSONItem(jsonObject.get(key));
				result.put(key, value);
			}
		}
		catch(JSONException ex) {
			throw new RuntimeException(ex);
		}
		return result;
	}

	/** Returns a List created by parsing the string argument as a JSON array and calling listFromJSONArray.
	 */
	public static List listFromJSONString(String jsonString) {
		try {
			return listFromJSONArray(new JSONArray(jsonString));
		} 
		catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/** Returns a Map created by parsing the string argument as a JSON object and calling mapFromJSONObject.
	 */
	public static Map mapFromJSONString(String jsonString) {
		try {
			return mapFromJSONObject(new JSONObject(jsonString));
		} 
		catch (JSONException ex) {
			throw new RuntimeException(ex);
		}
	}

}
