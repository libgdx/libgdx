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

package com.badlogic.gdx.backends.gwt;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextArea;

public class GwtApplicationConfiguration {
	/** the width of the drawing area in pixels **/
	public int width;
	/** the height of the drawing area in pixels **/
	public int height;
	/** whether to use a stencil buffer **/
	public boolean stencil = false;
	/** whether to enable antialiasing **/
	public boolean antialiasing = false;
	/** the framerate to run the game at **/
	public int fps = 60;
	/** the Panel to add the WebGL canvas to, can be null in which case a Panel is added automatically to the body element of the
	 * DOM **/
	public Panel rootPanel;
	/** the id of a canvas element to be used as the drawing area, can be null in which case a Panel and Canvas are added to the
	 * body element of the DOM **/
	public String canvasId;
	/** a TextArea to log messages to, can be null in which case a TextArea will be added to the body element of the DOM. */
	public TextArea log;
	/** whether to use debugging mode for OpenGL calls. Errors will result in a RuntimeException being thrown. */
	public boolean useDebugGL = false;

	public GwtApplicationConfiguration (int width, int height) {
		this.width = width;
		this.height = height;
	}
}
