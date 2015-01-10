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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.awt.Canvas;
import java.awt.Toolkit;
import java.nio.ByteBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

/** An implementation of the {@link Graphics} interface based on Lwjgl.
 * @author mzechner */
public class LwjglGraphics implements Graphics {
	static int major, minor;

	GL20 gl20;
	GL30 gl30;
	long frameId = -1;
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
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	boolean softwareMode;

	LwjglGraphics (LwjglApplicationConfiguration config) {
		this.config = config;
	}

	LwjglGraphics (Canvas canvas) {
		this.config = new LwjglApplicationConfiguration();
		config.width = canvas.getWidth();
		config.height = canvas.getHeight();
		this.canvas = canvas;
	}

	LwjglGraphics (Canvas canvas, LwjglApplicationConfiguration config) {
		this.config = config;
		this.canvas = canvas;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	public int getHeight () {
		if (canvas != null)
			return Math.max(1, canvas.getHeight());
		else
			return (int)(Display.getHeight() * Display.getPixelScaleFactor());
	}

	public int getWidth () {
		if (canvas != null)
			return Math.max(1, canvas.getWidth());
		else
			return (int)(Display.getWidth() * Display.getPixelScaleFactor());
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public long getFrameId () {
		return frameId;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	public float getRawDeltaTime () {
		return deltaTime;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public int getFramesPerSecond () {
		return fps;
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
		if(config.useHDPI) {
			System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
		}
		
		if (canvas != null) {
			Display.setParent(canvas);
		} else {
			boolean displayCreated = setDisplayMode(config.width, config.height, config.fullscreen);			
			if (!displayCreated) {
				if (config.setDisplayModeCallback != null) {
					config = config.setDisplayModeCallback.onFailure(config);
					if (config != null) {
						displayCreated = setDisplayMode(config.width, config.height, config.fullscreen);
					}
				}
				if (!displayCreated) {
					throw new GdxRuntimeException("Couldn't set display mode " + config.width + "x" + config.height + ", fullscreen: "
						+ config.fullscreen);
				}
			}
			if (config.iconPaths.size > 0) {
				ByteBuffer[] icons = new ByteBuffer[config.iconPaths.size];
				for (int i = 0, n = config.iconPaths.size; i < n; i++) {
					Pixmap pixmap = new Pixmap(Gdx.files.getFileHandle(config.iconPaths.get(i), config.iconFileTypes.get(i)));
					if (pixmap.getFormat() != Format.RGBA8888) {
						Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
						rgba.drawPixmap(pixmap, 0, 0);
						pixmap = rgba;
					}
					icons[i] = ByteBuffer.allocateDirect(pixmap.getPixels().limit());
					icons[i].put(pixmap.getPixels()).flip();
					pixmap.dispose();
				}
				Display.setIcon(icons);
			}
		}
		Display.setTitle(config.title);
		Display.setResizable(config.resizable);
		Display.setInitialBackground(config.initialBackgroundColor.r, config.initialBackgroundColor.g,
			config.initialBackgroundColor.b);

		Display.setLocation(config.x, config.y);
		createDisplayPixelFormat();
		initiateGLInstances();
	}

	private void createDisplayPixelFormat () {
		try {
			if (config.useGL30) {
				ContextAttribs context = new ContextAttribs(3, 2).withForwardCompatible(false).withProfileCore(true);
				try {
					Display.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil,
						config.samples), context);
				} catch (Exception e) {
					System.out.println("LwjglGraphics: couldn't create OpenGL 3.2+ core profile context");
					throw e;
				}
				System.out.println("LwjglGraphics: created OpenGL 3.2+ core profile context. This is experimental!");
			} else {
				Display
					.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil, config.samples));
			}
			bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples,
				false);
		} catch (Exception ex) {
			Display.destroy();
			try {
				Thread.sleep(200);
			} catch (InterruptedException ignored) {
			}
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
					Thread.sleep(200);
				} catch (InterruptedException ignored) {
				}
				try {
					Display.create(new PixelFormat());
				} catch (Exception ex3) {
					if (!softwareMode && config.allowSoftwareMode) {
						softwareMode = true;
						System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
						createDisplayPixelFormat();
						return;
					}
					String glInfo = glInfo();
					throw new GdxRuntimeException("OpenGL is not supported by the video driver"
						+ (glInfo.isEmpty() ? "." : (":" + glInfo())), ex3);
				}
				if (getDesktopDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 8, 0, 0, false);
				}
				if (getDesktopDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 8, 0, 0, false);
				}
				if (getDesktopDisplayMode().bitsPerPixel == 32) {
					bufferFormat = new BufferFormat(8, 8, 8, 8, 8, 0, 0, false);
				}
			}
		}
	}

	public void initiateGLInstances () {
		String version = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		major = Integer.parseInt("" + version.charAt(0));
		minor = Integer.parseInt("" + version.charAt(2));

		if (major >= 3) {
			gl30 = new LwjglGL30();
			gl20 = gl30;
		} else {
			gl20 = new LwjglGL20();
		}

		if (major <= 1)
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version);
		if (major == 2 || version.contains("2.1")) {
			if (!supportsExtension("GL_EXT_framebuffer_object") && !supportsExtension("GL_ARB_framebuffer_object")) {
				String glInfo = glInfo();
				throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: " + version
					+ ", FBO extension: false" + (glInfo.isEmpty() ? "" : ("\n" + glInfo())));
			}
		}

		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		Gdx.gl30 = gl30;
	}

	private String glInfo () {
		try {
			return GL11.glGetString(GL11.GL_VENDOR) + "\n" //
				+ GL11.glGetString(GL11.GL_RENDERER) + "\n" //
				+ GL11.glGetString(GL11.GL_VERSION);
		} catch (Throwable ignored) {
		}
		return "";
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
		if (config.overrideDensity != -1) return config.overrideDensity / 160f;
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
			float scaleFactor = Display.getPixelScaleFactor();
			config.width = (int)(mode.getWidth() * scaleFactor);
			config.height = (int)(mode.getHeight() * scaleFactor);
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
			resize = true;
			return true;
		} catch (LWJGLException e) {
			return false;
		}
	}

	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setDisplayMode (int width, int height, boolean fullscreen) {
		if (getWidth() == width && getHeight() == height && Display.isFullscreen() == fullscreen) {
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
			Display.setResizable(!fullscreen && config.resizable);
			
			float scaleFactor = Display.getPixelScaleFactor();
			config.width = (int)(targetDisplayMode.getWidth() * scaleFactor);
			config.height = (int)(targetDisplayMode.getHeight() * scaleFactor);
			if (Gdx.gl != null) Gdx.gl.glViewport(0, 0, config.width, config.height);
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
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	@Override
	public void setVSync (boolean vsync) {
		this.vsync = vsync;
		Display.setVSyncEnabled(vsync);
	}

	@Override
	public boolean supportsExtension (String extension) {
		if (extensions == null) extensions = gl20.glGetString(GL20.GL_EXTENSIONS);
		return extensions.contains(extension);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
		this.isContinuous = isContinuous;
	}

	@Override
	public boolean isContinuousRendering () {
		return isContinuous;
	}

	@Override
	public void requestRendering () {
		synchronized (this) {
			requestRendering = true;
		}
	}

	public boolean shouldRender () {
		synchronized (this) {
			boolean rq = requestRendering;
			requestRendering = false;
			return rq || isContinuous || Display.isDirty();
		}
	}

	@Override
	public boolean isFullscreen () {
		return Display.isFullscreen();
	}

	public boolean isSoftwareMode () {
		return softwareMode;
	}

	@Override
	public boolean isGL30Available () {
		return gl30 != null;
	}

	@Override
	public GL30 getGL30 () {
		return gl30;
	}

	/** A callback used by LwjglApplication when trying to create the display */
	public interface SetDisplayModeCallback {
		/** If the display creation fails, this method will be called. Suggested usage is to modify the passed configuration to use a
		 * common width and height, and set fullscreen to false.
		 * @return the configuration to be used for a second attempt at creating a display. A null value results in NOT attempting
		 *         to create the display a second time */
		public LwjglApplicationConfiguration onFailure (LwjglApplicationConfiguration initialConfig);
	}
}
