
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.jglfw.JglfwGraphics.JglfwDisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/** @author Nathan Sweet */
public class JglfwApplicationConfiguration {
	/** Title of application window **/
	public String title = "";
	/** Width & height of application window **/
	public int width = 640, height = 480;
	/** The x & y of application window, -1 for center **/
	public int x = -1, y = -1;
	/** True to start in fullscreen **/
	public boolean fullscreen;
	/** Monitor index to use for fullscreen **/
	public int fullscreenMonitorIndex = -1;
	/** Number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** Number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** Number of samples for MSAA **/
	public int samples = 0;
	/** True to enable vsync, can be changed at runtime via {@link Graphics#setVSync(boolean)} **/
	public boolean vSync = true;
	/** True if the window is resizable. **/
	public boolean resizable = true;
	/** True to attempt to use OpenGL ES 2.0. Note GL2 may be unavailable even when this is true. **/
	public boolean useGL20;
	/** True to call System.exit() when the main loop is complete. **/
	public boolean forceExit = true;
	/** True to have a title and border around the window. **/
	public boolean undecorated;
	/** Causes the main loop to run on the EDT instead of a new thread, for easier interoperability with AWT/Swing. **/
	public boolean runOnEDT;
	/** The color to clear the window immediately after creation. **/
	public Color initialBackgroundColor = Color.BLACK;
	/** True to hide the window when it is created. The window must be shown with {@link JglfwGraphics#show()}. **/
	public boolean hidden;

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
