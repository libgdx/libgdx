package com.badlogic.gdx.backends.jogl;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.backends.jogl.JoglGraphics.JoglDisplayMode;

public class JoglApplicationConfiguration {
	/** whether to use OpenGL ES 2.0 or not. default: false **/
	public boolean useGL20 = false;	
	/** number of bits per color channel **/
	public int r = 8, g = 8, b = 8, a = 8;
	/** number of bits for depth and stencil buffer **/
	public int depth = 16, stencil = 0;
	/** number of samples for MSAA **/
	public int samples = 0;
	/** width & height of application **/
	public int width = 480, height = 320;
	/** fullscreen **/
	public boolean fullscreen = false;
	/** title of application **/
	public String title = "Jogl Application";	
	
	/**
	 * Sets the r, g, b and a bits per channel based on the given
	 * {@link DisplayMode} and sets the fullscreen flag to true.
	 * @param mode
	 */
	public void setFromDisplayMode(DisplayMode mode) {
		this.width = mode.width;
		this.height = mode.height;
		if(mode.bitsPerPixel == 16) {
			this.r = 5;
			this.g = 6;
			this.b = 5;
			this.a = 0;
		}
		if(mode.bitsPerPixel == 24) {
			this.r = 8;
			this.g = 8;
			this.b = 8;
			this.a = 0;
		}
		if(mode.bitsPerPixel == 32) {
			this.r = 8;
			this.g = 8;
			this.b = 8;
			this.a = 8;
		}
		this.fullscreen = true;
	}
	
	public static DisplayMode getDesktopDisplayMode () {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		java.awt.DisplayMode mode = device.getDisplayMode();
		return new JoglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth(), mode);
	}
	
	public static DisplayMode[] getDisplayModes() {
		GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = genv.getDefaultScreenDevice();
		java.awt.DisplayMode desktopMode = device.getDisplayMode();
		java.awt.DisplayMode[] displayModes = device.getDisplayModes();
		ArrayList<DisplayMode> modes = new ArrayList<DisplayMode>();
		int idx = 0;
		for(java.awt.DisplayMode mode: displayModes) {
			boolean duplicate = false;
			for(int i = 0; i < modes.size(); i++) {										
				if(modes.get(i).width == mode.getWidth() && 
					modes.get(i).height == mode.getHeight() && 
					modes.get(i).bitsPerPixel == mode.getBitDepth()) {
					duplicate = true;
					break;
				}
			}
			if(duplicate) continue;
			if(mode.getBitDepth() != desktopMode.getBitDepth()) continue;
			modes.add(new JoglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRefreshRate(), mode.getBitDepth(), mode));
		}
		
		return modes.toArray(new DisplayMode[modes.size()]);
	}
}
