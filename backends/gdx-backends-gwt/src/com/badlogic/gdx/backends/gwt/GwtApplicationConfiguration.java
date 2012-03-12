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
	public int width;
	public int height;
	public boolean stencil = false;
	public boolean antialiasing = false;
	public int fps = 60;
	public Panel rootPanel;
	public TextArea log;
	public boolean useDebugGL = true;

	public GwtApplicationConfiguration (int width, int height) {
		this.width = width;
		this.height = height;
	}
}