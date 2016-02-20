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

package com.badlogic.gdx.backends.lwjgl3;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3DisplayMode;
import com.badlogic.gdx.graphics.Color;

public class Lwjgl3WindowConfiguration {
	int windowX = -1;
	int windowY = -1;
	int windowWidth = 640;
	int windowHeight = 480;
	boolean windowResizable = true;
	boolean windowDecorated = true;
	Lwjgl3WindowListener windowListener;
	Lwjgl3DisplayMode fullscreenMode;
	String title = "";
	Color initialBackgroundColor = Color.BLACK;
	
	/**
	 * Sets the app to use windowed mode.
	 * 
	 * @param width
	 *            the width of the window (default 640)
	 * @param height
	 *            the height of the window (default 480)
	 */
	public void setWindowedMode(int width, int height) {
		this.windowWidth = width;
		this.windowHeight = height;		
	}
	
	/** 
	 * @param resizable whether the windowed mode window is resizable
	 */
	public void setResizable(boolean resizable) {
		this.windowResizable = resizable;
	}
	
	/**
	 * @param decorated whether the windowed mode window is decorated, i.e. displaying the title bars (default true)
	 */
	public void setDecorated(boolean decorated) {
		this.windowDecorated = decorated;
	}
	
	/**
	 * Sets the position of the window in windowed mode on the
	 * primary monitor. Default -1 for booth coordinates for centered.
	 */
	public void setWindowPosition(int x, int y) {
		windowX = x;
		windowY = y;
	}
	
	/**
	 * Sets the {@link Lwjgl3WindowListener} which will be informed about
	 * iconficiation, focus loss and window close events.
	 */
	public void setWindowListener(Lwjgl3WindowListener windowListener) {
		this.windowListener = windowListener;
	}

	/**
	 * Sets the app to use fullscreen mode. Use the static methods like
	 * {@link #getDisplayMode()} on this class to enumerate connected monitors
	 * and their fullscreen display modes.
	 */
	public void setFullscreenMode(DisplayMode mode) {
		this.fullscreenMode = (Lwjgl3DisplayMode)mode;
	}
	
	/**
	 * Sets the window title. Defaults to empty string.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Sets the initial background color. Defaults to black.
	 */
	public void setInitialBackgroundColor(Color color) {
		initialBackgroundColor = color;
	}
}
