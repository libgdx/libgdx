/*
 * Copyright 2008 Google Inc.
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

package com.badlogic.gdx.backends.gwt.widgets;

import com.google.gwt.user.client.Element;

/** An interface that defines the methods required to support automatic resizing of the Widget element. */
public interface ResizableWidget {
	/** Get the widget's element. */
	Element getElement ();

	/** Check if this widget is attached to the page.
	 * 
	 * @return true if the widget is attached to the page */
	boolean isAttached ();

	/** This method is called when the dimensions of the parent element change. Subclasses should override this method as needed.
	 * 
	 * @param width the new client width of the element
	 * @param height the new client height of the element */
	void onResize (int width, int height);
}
