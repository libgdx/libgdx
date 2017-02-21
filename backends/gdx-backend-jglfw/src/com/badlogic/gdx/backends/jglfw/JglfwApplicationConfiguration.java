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

package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.jglfw.JglfwGraphics.JglfwDisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/** @author Nathan Sweet */
public class JglfwApplicationConfiguration {
	/** Title of application window. **/
	public String title = "";
	/** Initial width of the application window. **/
	public int width = 640;
	/** Initial height of the application window. **/
	public int height = 480;
	/** Intial x coordinate of the application window, -1 for center. **/
	public int x = -1;
	/** Intial x coordinate of the application window, -1 for center. **/
	public int y = -1;
	/** True to start in fullscreen. **/
	public boolean fullscreen;
	/** Monitor index to use for fullscreen. **/
	public int fullscreenMonitorIndex = -1;
	/** Number of bits per color channel. **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** Number of bits for the depth buffer. **/
	public int depth = 16;
	/** Number of bits for the stencil buffer. **/
	public int stencil = 0;
	/** Number of samples for MSAA **/
	public int samples = 0;
	/** True to enable vsync. **/
	public boolean vSync = true;
	/** True if the window is resizable. **/
	public boolean resizable = true;
	/** True to call System.exit() when the main loop is complete. **/
	public boolean forceExit = true;
	/** True to have a title and border around the window. **/
	public boolean undecorated;
	/** Causes the main loop to run on the EDT instead of a new thread, for easier interoperability with AWT/Swing. Broken on Linux. **/
	public boolean runOnEDT;
	/** The color to clear the window immediately after creation. **/
	public Color initialBackgroundColor = Color.BLACK;
	/** True to hide the window when it is created. The window must be shown with {@link JglfwGraphics#show()}. **/
	public boolean hidden;
	/** Target framerate when the window is in the foreground. The CPU sleeps as needed. Use 0 to never sleep. **/
	public int foregroundFPS = 60;
	/** Target framerate when the window is in the background. The CPU sleeps as needed. Use 0 to never sleep, -1 to not render. **/
	public int backgroundFPS = 60;
	/** Target framerate when the window is hidden or minimized. The CPU sleeps as needed. Use 0 to never sleep, -1 to not render. **/
	public int hiddenFPS = -1;
	/** Prefrences location on desktop. Default: current directory + ".prefs" */
	public String preferencesLocation = ".prefs/";

	static public DisplayMode[] getDisplayModes () {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		java.awt.DisplayMode desktopMode = device.getDisplayMode();
		java.awt.DisplayMode[] displayModes = device.getDisplayModes();
		Array<DisplayMode> modes = new Array();
		outer:
		for (java.awt.DisplayMode mode : displayModes) {
			for (DisplayMode other : modes)
				if (other.width == mode.getWidth() && other.height == mode.getHeight() && other.bitsPerPixel == mode.getBitDepth())
					continue outer; // Duplicate.
			if (mode.getBitDepth() != desktopMode.getBitDepth()) continue;
			modes.add(new JglfwDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth()));
		}
		return modes.toArray(DisplayMode.class);
	}

	static public DisplayMode getDesktopDisplayMode () {
		java.awt.DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		return new JglfwDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth());
	}
}
