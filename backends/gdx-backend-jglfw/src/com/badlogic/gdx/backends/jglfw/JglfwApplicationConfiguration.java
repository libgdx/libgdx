
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.jglfw.JglfwGraphics.JglfwDisplayMode;
import com.badlogic.gdx.utils.Array;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

/** @author Nathan Sweet */
public class JglfwApplicationConfiguration {
	/** title of application window **/
	public String title = "Jglfw Application";
	/** width & height of application window **/
	public int width = 480, height = 320;
	/** x & y of application window, -1 for center **/
	public int x = -1, y = -1;
	/** whether to start in fullscreen **/
	public boolean fullscreen;
	/** monitor index to use for fullscreen **/
	public int fullscreenMonitorIndex = -1;
	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** number of samples for MSAA **/
	public int samples = 0;
	/** whether to enable vsync, can be changed at runtime via {@link Graphics#setVSync(boolean)} **/
	public boolean vSync = true;
	/** whether the window is resizable **/
	public boolean resizable = true;
	/** whether to attempt to use OpenGL ES 2.0. Note GL2 may be unavailable even when this is true. default: false **/
	public boolean useGL20 = false;
	/** whether to call System.exit() when the main loop exits **/
	public boolean forceExit = true;

	static public DisplayMode[] getDisplayModes () {
		// FIXME this should use GLFW methods on the current monitor in use
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
		// FIXME this should use GLFW APIs using the current monitor
		java.awt.DisplayMode mode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		return new JglfwDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth());
	}
}
