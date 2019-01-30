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

package com.badlogic.gdx.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.Net.HttpRequest;

/** Provides utility methods to work with the {@link HttpRequest} content and parameters. */
public class HttpParametersUtils {

	public static String defaultEncoding = "UTF-8";
	public static String nameValueSeparator = "=";
	public static String parameterSeparator = "&";

	/** Useful method to convert a map of key,value pairs to a String to be used as part of a GET or POST content.
	 * @param parameters A Map<String, String> with the parameters to encode.
	 * @return The String with the parameters encoded. */
	public static String convertHttpParameters (Map<String, String> parameters) {
		Set<String> keySet = parameters.keySet();
		StringBuilder convertedParameters = new StringBuilder();
		for (String name : keySet) {
			convertedParameters.append(encode(name, defaultEncoding));
			convertedParameters.append(nameValueSeparator);
			convertedParameters.append(encode(parameters.get(name), defaultEncoding));
			convertedParameters.append(parameterSeparator);
		}
		if (convertedParameters.length() > 0) convertedParameters.deleteCharAt(convertedParameters.length() - 1);
		return convertedParameters.toString();
	}

	private static String encode (String content, String encoding) {
		try {
			return URLEncoder.encode(content, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
