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

/** The JSONException is thrown by the JSON.org classes when things are amiss.
 * @author JSON.org
 * @version 2010-12-24 */
public class JSONException extends Exception {
	private static final long serialVersionUID = 0;
	private Throwable cause;

	/** Constructs a JSONException with an explanatory message.
	 * @param message Detail about the reason for the exception. */
	public JSONException (String message) {
		super(message);
	}

	public JSONException (Throwable cause) {
		super(cause.getMessage());
		this.cause = cause;
	}

	public Throwable getCause () {
		return this.cause;
	}
}
