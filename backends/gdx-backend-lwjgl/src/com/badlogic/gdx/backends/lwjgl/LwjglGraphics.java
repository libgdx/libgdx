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
import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.badlogic.gdx.AbstractGraphics;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.PixelFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.SharedLibraryLoader;

/** An implementation of the {@link Graphics} interface based on Lwjgl.
 * @author mzechner */
public class LwjglGraphics extends AbstractGraphics {

	/** The suppored OpenGL extensions */
	static Array<String> extensions;
	static GLVersion glVersion;

	GL20 gl20;
	GL30 gl30;
	long frameId = -1;
	float deltaTime;
	boolean resetDeltaTime;
	long frameStart;
	int frames;
	int fps;
	long lastTime = System.nanoTime();
	Canvas canvas;
	boolean vsync = false;
	boolean resize = false;
	LwjglApplicationConfiguration config;
	BufferFormat bufferFormat = new BufferFormat(8, 8, 8, 8, 16, 8, 0, false);
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	volatile boolean forceDisplayModeChange = false;
	boolean softwareMode;
	boolean usingGL30;

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
		this(config);
		this.canvas = canvas;
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

	@Override
	public int getBackBufferWidth () {
		return getWidth();
	}

	@Override
	public int getBackBufferHeight () {
		return getHeight();
	}

	public long getFrameId () {
		return frameId;
	}

	public float getDeltaTime () {
		return deltaTime;
	}

	/** The delta time for the next frame will be 0. This can be useful if the render thread was blocked for some time to prevent
	 * game state or animations from advancing. */
	public void resetDeltaTime () {
		resetDeltaTime = true;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public GLVersion getGLVersion () {
		return glVersion;
	}

	public boolean isGL20Available () {
		return gl20 != null;
	}

	public GL20 getGL20 () {
		return gl20;
	}

	@Override
	public void setGL20 (GL20 gl20) {
		this.gl20 = gl20;
		if (gl30 == null) {
			Gdx.gl = gl20;
			Gdx.gl20 = gl20;
		}
	}

	@Override
	public boolean isGL30Available () {
		return gl30 != null;
	}

	@Override
	public GL30 getGL30 () {
		return gl30;
	}

	@Override
	public void setGL30 (GL30 gl30) {
		this.gl30 = gl30;
		if (gl30 != null) {
			this.gl20 = gl30;

			Gdx.gl = gl20;
			Gdx.gl20 = gl20;
			Gdx.gl30 = gl30;
		}
	}

	public int getFramesPerSecond () {
		return fps;
	}

	void updateTime () {
		long time;
		if (resetDeltaTime) {
			resetDeltaTime = false;
			time = lastTime;
		} else
			time = System.nanoTime();
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
		if (config.useHDPI) {
			System.setProperty("org.lwjgl.opengl.Display.enableHighDPI", "true");
		}

		setUndecorated(config.undecorated);

		if (canvas != null) {
			Display.setParent(canvas);
		} else {
			boolean displayCreated = false;

			if (!config.fullscreen) {
				displayCreated = setWindowedMode(config.width, config.height);
			} else {
				DisplayMode bestMode = null;
				for (DisplayMode mode : getDisplayModes()) {
					if (mode.width == config.width && mode.height == config.height) {
						if (bestMode == null || bestMode.refreshRate < this.getDisplayMode().refreshRate) {
							bestMode = mode;
						}
					}
				}
				if (bestMode == null) {
					bestMode = this.getDisplayMode();
				}
				displayCreated = setFullscreenMode(bestMode);
			}
			if (!displayCreated) {
				if (config.setDisplayModeCallback != null) {
					config = config.setDisplayModeCallback.onFailure(config);
					if (config != null) {
						displayCreated = setWindowedMode(config.width, config.height);
					}
				}
				if (!displayCreated) {
					throw new GdxRuntimeException(
						"Couldn't set display mode " + config.width + "x" + config.height + ", fullscreen: " + config.fullscreen);
				}
			}
			if (config.iconPaths.size > 0) {
				ByteBuffer[] icons = new ByteBuffer[config.iconPaths.size];
				for (int i = 0, n = config.iconPaths.size; i < n; i++) {
					Pixmap pixmap = new Pixmap(Gdx.files.getFileHandle(config.iconPaths.get(i), config.iconFileTypes.get(i)));
					if (pixmap.getFormat() != Format.RGBA8888) {
						Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Format.RGBA8888);
						rgba.setBlending(Blending.None);
						rgba.drawPixmap(pixmap, 0, 0);
						pixmap.dispose();
						pixmap = rgba;
					}
					icons[i] = ByteBuffer.allocateDirect(pixmap.getPixels().limit());
					((Buffer)icons[i].put(pixmap.getPixels())).flip();
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
		createDisplayPixelFormat(config.useGL30, config.gles30ContextMajorVersion, config.gles30ContextMinorVersion);
		initiateGL();
	}

	/** Only needed when setupDisplay() is not called. */
	void initiateGL () {
		extractVersion();
		extractExtensions();
		initiateGLInstances();
	}

	private static void extractVersion () {
		String versionString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VERSION);
		String vendorString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_VENDOR);
		String rendererString = org.lwjgl.opengl.GL11.glGetString(GL11.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	private static void extractExtensions () {
		extensions = new Array<String>();
		if (glVersion.isVersionEqualToOrHigher(3, 2)) {
			int numExtensions = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
			for (int i = 0; i < numExtensions; ++i)
				extensions.add(org.lwjgl.opengl.GL30.glGetStringi(GL20.GL_EXTENSIONS, i));
		} else {
			extensions.addAll(org.lwjgl.opengl.GL11.glGetString(GL20.GL_EXTENSIONS).split(" "));
		}
	}

	/** @return whether the supported OpenGL (not ES) version is compatible with OpenGL ES 3.x. */
	private static boolean fullCompatibleWithGLES3 () {
		// OpenGL ES 3.0 is compatible with OpenGL 4.3 core, see http://en.wikipedia.org/wiki/OpenGL_ES#OpenGL_ES_3.0
		return glVersion.isVersionEqualToOrHigher(4, 3);
	}

	/** @return whether the supported OpenGL (not ES) version is compatible with OpenGL ES 2.x. */
	private static boolean fullCompatibleWithGLES2 () {
		// OpenGL ES 2.0 is compatible with OpenGL 4.1 core
		// see https://www.opengl.org/registry/specs/ARB/ES2_compatibility.txt
		return glVersion.isVersionEqualToOrHigher(4, 1) || extensions.contains("GL_ARB_ES2_compatibility", false);
	}

	private static boolean supportsFBO () {
		// FBO is in core since OpenGL 3.0, see https://www.opengl.org/wiki/Framebuffer_Object
		return glVersion.isVersionEqualToOrHigher(3, 0) || extensions.contains("GL_EXT_framebuffer_object", false)
			|| extensions.contains("GL_ARB_framebuffer_object", false);
	}

	/** @return whether cubemap seamless feature is supported. */
	public static boolean supportsCubeMapSeamless () {
		return glVersion.isVersionEqualToOrHigher(3, 2) || extensions.contains("GL_ARB_seamless_cube_map", false);
	}

	/** Enable or disable cubemap seamless feature. Default is true if supported. Should only be called if this feature is
	 * supported. (see {@link #supportsCubeMapSeamless()})
	 * @param enable */
	public void enableCubeMapSeamless (boolean enable) {
		if (enable) {
			gl20.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		} else {
			gl20.glDisable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		}
	}

	private void createDisplayPixelFormat (boolean useGL30, int gles30ContextMajor, int gles30ContextMinor) {
		try {
			if (useGL30) {
				ContextAttribs context = new ContextAttribs(gles30ContextMajor, gles30ContextMinor).withForwardCompatible(false)
					.withProfileCore(true);
				try {
					Display.create(
						new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil, config.samples),
						context);
				} catch (Exception e) {
					System.out.println("LwjglGraphics: OpenGL " + gles30ContextMajor + "." + gles30ContextMinor
						+ "+ core profile (GLES 3.0) not supported.");
					createDisplayPixelFormat(false, gles30ContextMajor, gles30ContextMinor);
					return;
				}
				System.out.println("LwjglGraphics: created OpenGL " + gles30ContextMajor + "." + gles30ContextMinor
					+ "+ core profile (GLES 3.0) context. This is experimental!");
				usingGL30 = true;
			} else {
				Display
					.create(new PixelFormat(config.r + config.g + config.b, config.a, config.depth, config.stencil, config.samples));
				usingGL30 = false;
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
				if (getDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 16, 8, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 16, 8, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 32) {
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
					Display.destroy();
					if (!softwareMode && config.allowSoftwareMode) {
						softwareMode = true;
						System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
						createDisplayPixelFormat(useGL30, gles30ContextMajor, gles30ContextMinor);
						return;
					}
					throw new GdxRuntimeException("OpenGL is not supported by the video driver.", ex3);
				}
				if (getDisplayMode().bitsPerPixel == 16) {
					bufferFormat = new BufferFormat(5, 6, 5, 0, 8, 0, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 24) {
					bufferFormat = new BufferFormat(8, 8, 8, 0, 8, 0, 0, false);
				}
				if (getDisplayMode().bitsPerPixel == 32) {
					bufferFormat = new BufferFormat(8, 8, 8, 8, 8, 0, 0, false);
				}
			}
		}
	}

	public void initiateGLInstances () {
		if (usingGL30) {
			gl30 = new LwjglGL30();
			gl20 = gl30;
		} else {
			gl20 = new LwjglGL20();
		}

		if (!glVersion.isVersionEqualToOrHigher(2, 0))
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
				+ GL11.glGetString(GL11.GL_VERSION) + "\n" + glVersion.getDebugVersionString());

		if (!supportsFBO()) {
			throw new GdxRuntimeException("OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: "
				+ GL11.glGetString(GL11.GL_VERSION) + ", FBO extension: false\n" + glVersion.getDebugVersionString());
		}

		Gdx.gl = gl20;
		Gdx.gl20 = gl20;
		Gdx.gl30 = gl30;

		if (supportsCubeMapSeamless()) {
			enableCubeMapSeamless(true);
		}
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
		return getPpiX() / 2.54f;
	}

	@Override
	public float getPpcY () {
		return getPpiY() / 2.54f;
	}

	@Override
	public float getDensity () {
		if (config.overrideDensity != -1) return config.overrideDensity / 160f;
		return super.getDensity();
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return true;
	}

	@Override
	public Monitor getPrimaryMonitor () {
		return new LwjglMonitor(0, 0, "Primary Monitor");
	}

	@Override
	public Monitor getMonitor () {
		return getPrimaryMonitor();
	}

	@Override
	public Monitor[] getMonitors () {
		return new Monitor[] {getPrimaryMonitor()};
	}

	@Override
	public DisplayMode[] getDisplayModes (Monitor monitor) {
		return getDisplayModes();
	}

	@Override
	public DisplayMode getDisplayMode (Monitor monitor) {
		return getDisplayMode();
	}

	@Override
	public int getSafeInsetLeft () {
		return 0;
	}

	@Override
	public int getSafeInsetTop () {
		return 0;
	}

	@Override
	public int getSafeInsetBottom () {
		return 0;
	}

	@Override
	public int getSafeInsetRight () {
		return 0;
	}

	@Override
	public boolean setFullscreenMode (DisplayMode displayMode) {
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
	public boolean setWindowedMode (int width, int height) {
		boolean displaySizeUnchanged = getWidth() == width && getHeight() == height && !Display.isFullscreen();
		if (displaySizeUnchanged && !forceDisplayModeChange) {
			return true;
		}

		this.forceDisplayModeChange = false;

		try {
			org.lwjgl.opengl.DisplayMode targetDisplayMode = null;
			boolean fullscreen = false;

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

			boolean resizable = !fullscreen && config.resizable;

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			// Workaround for bug in LWJGL whereby resizable state is lost on DisplayMode change
			if (resizable == Display.isResizable()) {
				Display.setResizable(!resizable);
			}
			Display.setResizable(resizable);

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
					modes[idx++] = new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(),
						mode);
				}
			}

			return modes;
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't fetch available display modes", e);
		}
	}

	@Override
	public DisplayMode getDisplayMode () {
		org.lwjgl.opengl.DisplayMode mode = Display.getDesktopDisplayMode();
		return new LwjglDisplayMode(mode.getWidth(), mode.getHeight(), mode.getFrequency(), mode.getBitsPerPixel(), mode);
	}

	@Override
	public void setTitle (String title) {
		Display.setTitle(title);
	}

	/** Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take effect. */
	@Override
	public void setUndecorated (boolean undecorated) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", undecorated ? "true" : "false");
		this.config.undecorated = undecorated;
		this.forceDisplayModeChange = true;
	}

	/** Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take effect. */
	@Override
	public void setResizable (boolean resizable) {
		this.config.resizable = resizable;
		Display.setResizable(resizable);
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

	/** Sets the target framerate for the application, when using continuous rendering. Must be positive. The cpu sleeps as needed.
	 * Use 0 to never sleep. Default is 60.
	 *
	 * @param fps fps */
	@Override
	public void setForegroundFPS (int fps) {
		this.config.foregroundFPS = fps;
	}

	@Override
	public boolean supportsExtension (String extension) {
		return extensions.contains(extension, false);
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

	/** A callback used by LwjglApplication when trying to create the display */
	public interface SetDisplayModeCallback {
		/** If the display creation fails, this method will be called. Suggested usage is to modify the passed configuration to use
		 * a common width and height, and set fullscreen to false.
		 * @return the configuration to be used for a second attempt at creating a display. A null value results in NOT attempting
		 *         to create the display a second time */
		public LwjglApplicationConfiguration onFailure (LwjglApplicationConfiguration initialConfig);
	}

	@Override
	public com.badlogic.gdx.graphics.Cursor newCursor (Pixmap pixmap, int xHotspot, int yHotspot) {
		return new LwjglCursor(pixmap, xHotspot, yHotspot);
	}

	@Override
	public void setCursor (com.badlogic.gdx.graphics.Cursor cursor) {
		if (canvas != null && SharedLibraryLoader.isMac) {
			return;
		}
		try {
			Mouse.setNativeCursor(((LwjglCursor)cursor).lwjglCursor);
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Could not set cursor image.", e);
		}
	}

	@Override
	public void setSystemCursor (SystemCursor systemCursor) {
		if (canvas != null && SharedLibraryLoader.isMac) {
			return;
		}
		try {
			Mouse.setNativeCursor(null);
		} catch (LWJGLException e) {
			throw new GdxRuntimeException("Couldn't set system cursor");
		}
	}

	private class LwjglDisplayMode extends DisplayMode {
		org.lwjgl.opengl.DisplayMode mode;

		public LwjglDisplayMode (int width, int height, int refreshRate, int bitsPerPixel, org.lwjgl.opengl.DisplayMode mode) {
			super(width, height, refreshRate, bitsPerPixel);
			this.mode = mode;
		}
	}

	private class LwjglMonitor extends Monitor {
		protected LwjglMonitor (int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
		}
	}
}
