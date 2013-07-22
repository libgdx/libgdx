/*******************************************************************************
 * Copyright 2012 See AUTHORS file.
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

package com.badlogic.gdx.backends.gwt.widgets;

import com.google.gwt.user.client.ui.TextBox;

public class PlaceholderTextBox extends TextBox {

	String placeholder = "";

	/** Creates an empty text box. */
	public PlaceholderTextBox () {
	}

	/** Gets the current placeholder text for the text box.
	 * 
	 * @return the current placeholder text */
	public String getPlaceholder () {
		return placeholder;
	}

	/** Sets the placeholder text displayed in the text box.
	 * 
	 * @param text the placeholder text */
	public void setPlaceholder (String text) {
		placeholder = (text != null ? text : "");
		getElement().setPropertyString("placeholder", placeholder);
	}
}
