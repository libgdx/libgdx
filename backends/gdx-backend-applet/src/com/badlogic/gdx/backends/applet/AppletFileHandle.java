/*******************************************************************************
 * Copyright 2010 Mario Zechner (contact@badlogicgames.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.applet;

import java.io.InputStream;

import com.badlogic.gdx.files.FileHandle;

/**
 * A {@link FileHandle} implementation for the desktop.
 * 
 * @author mzechner
 * 
 */
public class AppletFileHandle implements FileHandle {
	/** the file **/
	private final String uri;

	AppletFileHandle (String uri) {
		this.uri = uri;
	}

	/**
	 * @return the underlying {@link InputStream}. Needs to be closed when the handle is no longer used.
	 */
	public InputStream getInputStream () {
		return getClass().getResourceAsStream("/" + uri);
	}
}
