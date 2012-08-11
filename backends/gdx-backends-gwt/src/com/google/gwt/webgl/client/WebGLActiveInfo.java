/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.webgl.client;

/** The WebGLActiveInfo interface represents the information returned from the getActiveAttrib and getActiveUniform calls. */
public class WebGLActiveInfo extends WebGLObject {

	protected WebGLActiveInfo () {
	}

	/** Gets the size of the requested variable. */
	public final native int getSize () /*-{
													return this.size;
													}-*/;

	/** Gets the name of the requested variable. */
	public final native String getName () /*-{
														return this.name;
														}-*/;

	public final native int getType () /*-{
													return this.type;
													}-*/;
}
