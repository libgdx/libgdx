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
package com.badlogic.gdx.scenes.scene2d.ui.utils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A very simple clipboard interface for text content.
 * @author mzechner
 *
 */
public abstract class Clipboard {
	/** 
	 * gets the current content of the clipboard if it contains text
	 * @return the clipboard content or null
	 */
	public abstract String getContents();
	
	/**
	 * Sets the content of the system clipboard.
	 * @param content the content
	 */
	public abstract void setContents(String content);
	
	public static Clipboard getDefaultClipboard() {
		if(Gdx.app.getType() == ApplicationType.Android) return new AndroidClipboard();
		else {
			try {
				return (Clipboard)(Class.forName("com.badlogic.gdx.scenes.scene2d.ui.utils.DesktopClipboard").newInstance());
			} catch(Exception e) {
				throw new GdxRuntimeException("Couldn't instantiate desktop clipboard", e);
			}
		}
	}
}
