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

package com.badlogic.gdx.backends.lwjgl;

import java.awt.Canvas;
import java.awt.Toolkit;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.GLU;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** An implementation of the {@link Graphics} interface based on Lwjgl.
 * @author mzechner */
public final class LwjglGraphics implements Graphics {
	static int major, minor;

	GLCommon gl;
	GL10 gl10;
	GL11 gl11;
	GL20 gl20;
	GLU glu;
	float deltaTime = 0;
	long frameStart = 0;
	int frames = 0;
	int fps;
	long lastTime = System.nanoTime();
	Canvas canvas;
	boolean vsync = false;
	boolean resize = false;
	LwjglApplicationConfiguration config;
	BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
	String extensions;

	LwjglGraphics (LwjglApplicationConfiguration config) {
		this.config = config;
	}

	LwjglGraphics (Canvas canvas, boolean useGL2IfAvailable) {
		this.config = new LwjglApplicationConfiguration();
		config.width = canvas.getWidth();
		config.height = canvas.getHeight();
		config.useGL20 = useGL2IfAvailable;
		this.canvas = canvas;
	}

	public GL10 getGL10 () {
		return gl10;
	}

	public GL11 getGL11 () {
		return gl11;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public GLU getGLU () {
		return glu;
	}

	public int getHeight () {
		if (canvas != null)
			return canvas.getHeight();
		else
			return config.height;
	}

	public int getWidth () {
		if (canvas != null)
			return canvas.getWidth();
		else
			return config.width;
	}

	public boolean isGL11Available () {
		return gl11 != null;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public int getFramesPerSecond () {
		return fps;
	}

	@Override
	public GLCommon getGLCommon () {
		return gl;
	}

	void updateTime () {
		long time = System.nanoTime();
		deltaTime = (time - lastTime) / 1000000000.0f;
		lastTime = time;

		if (time - frameStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameStart = time;
		}
		frames++;
	}

	void setupDisplay () throws LWJGLException {
		if (canvas != null) {
			Display.setParent(canvas);
		} else {
			if (!setDisplayMode(config.width, config.height, config.fullscreen))
				throw new GdxRuntimeException("Couldn't set display mode " + config.width + "x" + config.height + ", fullscreen: "
					+ config.fullscreen);
		}
		Display.setTitle(config.title);
		createDisplayPixelFormat();
		initiateGLInstances();
	}

	private void createDisplayPixelFormat () {
		int samples = 0;
		try {
			Display.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil, config.samples));
			bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples,
				false);
		} catch (Exception ex) {
			Display.destroy();
			try {
				Display.create(new PixelFormat(0, 16, 8));
				if (getDesktopDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 8, 0, false);
				}
				if (getDesktopDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 16, 8, 0, false);
				}
				if (getDesktopDisplayMode().bitsPerPixel == 32) {
					bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
				}
			} catch (Exception ex2) {
				Display.destroy();
				try {
					Display.create(new PixelFormat());
					if (getDesktopDisplayMode().bitsPerPixel == 16) {
						bufferFormat = new BufferFormat(5, 6, 5, 0, 8, 0, 0, false);
					}
					if (getDesktopDisplayMode().bitsPerPixel == 24) {
						bufferFormat = new BufferFormat(8, 8, 8, 0, 8, 0, 0, false);
					}
					if (getDesktopDisplayMode().bitsPerPixel == 32) {
						bufferFormat = new BufferFormat(8, 8, 8, 8, 8, 0, 0, false);
					}
				} catch (Exception ex3) {
					if (ex3.getMessage().contains("Pixel format not accelerated"))
						throw new GdxRuntimeException("OpenGL is not supported by the video driver.", ex3);
				}
			}
		}
	}

	private void initiateGLInstances () {
		String version = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		major = Integer.parseInt("" + version.charAt(0));
		minor = Integer.parseInt("" + version.charAt(2));

		if (config.useGL20 && major >= 2) {
			// FIXME add check whether gl 2.0 is supported
			gl20 = new LwjglGL20();
			gl = gl20;
		} else {
			if (major == 1 && minor < 5) {
				gl10 = new LwjglGL10();
			} else {
				gl11 = new LwjglGL11();
				gl10 = gl11;
			}
			gl = gl10;
		}

		glu = new LwjglGLU();

		Gdx.glu = glu;
		Gdx.gl = gl;
		Gdx.gl10 = gl10;
		Gdx.gl11 = gl11;
		Gdx.gl20 = gl20;
	}

	@Override
	public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getPpcY () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f);
	}

	@Override
	public float getDensity () {
		return (Toolkit.getDefaultToolkit().getScreenResolution() / 160f);
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return true;
	}

	private class LwjglDisplayMode extends DisplayMode {
		org.lwjgl.opengl.DisplayMode mode;

		public LwjglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, org.lwjgl.opengl.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}

	}

	@Override
	public boolean setDisplayMode (DisplayMode displayMode) {
		org.lwjgl.opengl.DisplayMode mode = ((LwjglDisplayMode)displayMode).mode;
		try {
			if (!mode.isFullscreenCapable()) {
				Display.setDisplayMode(mode);
			} else {
				Display.setDisplayModeAndFullscreen(mode);
			}
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, displayMode.width, displayMode.height);
			config.width = displayMode.width;
			config.height = displayMode.height;
			resize = true;
			return true;
		} catch (LWJGLException e) {
			return false;
		}
	}

	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		if ((Display.getDisplayMode().getWidth() == width) && (Display.getDisplayMode().getHeight() == height)
			&& (Display.isFullscreen() == fullscreen)) {
			return true;
		}

		try {
			org.lwjgl.opengl.DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				org.lwjgl.opengl.DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++) {
					org.lwjgl.opengl.DisplayMode current = modes[i];

					if ((current.getWidth() == width) && (current.getHeight() == height)) {
						if ((targetDisplayMode == null) || (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null) || (current.getBitsPerPixel() > targetDisplayMode.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against the
						// original display mode then it's probably best to go for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display.getDesktopDisplayMode().getBitsPerPixel())
							&& (current.getFrequency() == Display.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new org.lwjgl.opengl.DisplayMode(width, height);
			}

			if (targetDisplayMode == null) {
				return false;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, targetDisplayMode.getWidth(), targetDisplayMode.getHeight());
			config.width = targetDisplayMode.getWidth();
			config.height = targetDisplayMode.getHeight();
			resize = true;
			return true;
		} catch (LWJGLException e) {
			return false;
		}
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		try {
			org.lwjgl.opengl.DisplayMode[] availableDisplayModes = Display.getAvailableDisplayModes();
			DisplayMode[] modes = new DisplayMode[availableDisplayModes.length];

			int idx = 0;
			for (org.lwjgl.opengl.DisplayMode mode : availableDisplayModes) {
				if (mode.isFullscreenCapable()) {
					modes[idx++] = new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(),
						mode.getBitsPerPixel(), mode);
				}
			}

			return modes;
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't fetch available display modes", e);
		}
	}

	@Override
	public DisplayMode getDesktopDisplayMode () {
		org.lwjgl.opengl.DisplayMode mode = Display.getDesktopDisplayMode();
		return new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(), mode);
	}

	@Override
	public void setTitle (String title) {
		Display.setTitle(title);
	}

	@Override
	public void setIcon (Pixmap pixmap) {
		Display.setIcon(new ByteBuffer[] {pixmap.getPixels().duplicate()});
	}

	@Override
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	@Override
	public void setVSync (boolean vsync) {
		this.vsync = vsync;
		if (vsync && !config.useCPUSynch) Display.setVSyncEnabled(true);
		if (!vsync && !config.useCPUSynch) Display.setVSyncEnabled(false);
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = Gdx.gl.glGetString(GL10.GL_EXTENSIONS);
		return extensions.contains(extension);
	}
}
