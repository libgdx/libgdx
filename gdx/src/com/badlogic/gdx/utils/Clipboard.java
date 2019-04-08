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

package com.badlogic.gdx.utils;

/** A very simple clipboard interface for text content.
 * @author mzechner */
public interface Clipboard {
	/** gets the current content of the clipboard if it contains text
	 * for WebGL app, getting the system clipboard is currently not supported. It works only inside the app
	 * @return the clipboard content or null */
	public String getContents ();

	/** Sets the content of the system clipboard.
	 * for WebGL app, clipboard content might not be set if user denied permission, setting clipboard is not synchronous
	 * so you can't rely on getting same content just after setting it
	 * @param content the content */
	public void setContents (String content);
}
