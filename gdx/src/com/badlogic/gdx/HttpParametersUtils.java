package com.badlogic.gdx;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net.HttpRequest;

/** Provides utility methods to work with the {@link HttpRequest} content and parameters. */
public class HttpParametersUtils {

	/** Useful method to convert a map of key,value pairs to a String to be used as part of a GET or POST content.
	 * @param parameters A Map<String, String> with the parameters to encode.
	 * @return The String with the parameters encoded. */
	public static String convertHttpParameters (Map<String, String> parameters) {
		Set<String> keySet = parameters.keySet();
		String convertedParameters = "";
		for (String name : keySet)
			convertedParameters += name + "=" + parameters.get(name) + "&";
		return convertedParameters;
	}

}