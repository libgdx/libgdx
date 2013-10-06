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

/*
 Copyright (c) 2006 JSON.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 The Software shall be used for Good, not Evil.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

import java.io.StringWriter;

/** JSONStringer provides a quick and convenient way of producing JSON text. The texts produced strictly conform to JSON syntax
 * rules. No whitespace is added, so the results are ready for transmission or storage. Each instance of JSONStringer can produce
 * one JSON text.
 * <p>
 * A JSONStringer instance provides a <code>value</code> method for appending values to the text, and a <code>key</code> method
 * for adding keys before values in objects. There are <code>array</code> and <code>endArray</code> methods that make and bound
 * array values, and <code>object</code> and <code>endObject</code> methods which make and bound object values. All of these
 * methods return the JSONWriter instance, permitting cascade style. For example,
 * 
 * <pre>
 * myString = new JSONStringer().object().key(&quot;JSON&quot;).value(&quot;Hello, World!&quot;).endObject().toString();
 * </pre>
 * 
 * which produces the string
 * 
 * <pre>
 * {"JSON":"Hello, World!"}
 * </pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>. There are no methods for adding commas or colons.
 * JSONStringer adds them for you. Objects and arrays can be nested up to 20 levels deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.
 * @author JSON.org
 * @version 2008-09-18 */
public class JSONStringer extends JSONWriter {
	/** Make a fresh JSONStringer. It can be used to build one JSON text. */
	public JSONStringer () {
		super(new StringWriter());
	}

	/** Return the JSON text. This method is used to obtain the product of the JSONStringer instance. It will return
	 * <code>null</code> if there was a problem in the construction of the JSON text (such as the calls to <code>array</code> were
	 * not properly balanced with calls to <code>endArray</code>).
	 * @return The JSON text. */
	public String toString () {
		return this.mode == 'd' ? this.writer.toString() : null;
	}
}
