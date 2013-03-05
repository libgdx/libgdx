
package com.badlogic.gdx.backends.jglfw;

import com.badlogic.gdx.Graphics;

/** @author Nathan Sweet */
public class JglfwApplicationConfiguration {
	/** title of application window **/
	public String title;
	/** width & height of application window **/
	public int width, height;
	/** x & y of application window, -1 for center **/
	public int x = -1, y = -1;
	/** whether to start in fullscreen **/
	public boolean fullscreen;
	/** monitor index to use for fullscreen **/
	public int fullscreenMonitorIndex;
	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** number of samples for MSAA **/
	public int samples = 0;
	/** bit depth */
	public int bitsPerPixel = 24;
	/** whether to enable vsync, can be changed at runtime via {@link Graphics#setVSync(boolean)} **/
	public boolean vSync = true;
	/** whether to sleep the CPU instead of vsync when vSync is enabled **/
	public boolean cpuSync = true;
	/** whether the window is resizable **/
	public boolean resizable = true;
	/** whether to attempt to use OpenGL ES 2.0. Note GL2 may be unavailable even when this is true. default: false **/
	public boolean useGL20 = false;
	/** whether to call System.exit() when the main loop exits **/
	public boolean forceExit = true;
}
