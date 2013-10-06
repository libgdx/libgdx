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

/** The <code>JSONString</code> interface allows a <code>toJSONString()</code> method so that a class can change the behavior of
 * <code>JSONObject.toString()</code>, <code>JSONArray.toString()</code>, and <code>JSONWriter.value(</code>Object<code>)</code>.
 * The <code>toJSONString</code> method will be used instead of the default behavior of using the Object's <code>toString()</code>
 * method and quoting the result. */
public interface JSONString {
	/** The <code>toJSONString</code> method allows a class to produce its own JSON serialization.
	 * 
	 * @return A strictly syntactically correct JSON text. */
	public String toJSONString ();
}
