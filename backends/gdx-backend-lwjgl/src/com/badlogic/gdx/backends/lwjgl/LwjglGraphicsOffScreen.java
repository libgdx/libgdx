/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.backends.lwjgl;

import java.awt.*;

import org.lwjgl.*;
import org.lwjgl.opengl.*;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Cursor.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.*;
import com.badlogic.gdx.utils.*;

/** LwjglGraphicsOffScreen implementation derived from {@link LwjglGraphics}. This class uses an external GL context.
 * 
 * @author ClassX */
public class LwjglGraphicsOffScreen extends AbstractGraphics {
	/** The supported OpenGL extensions */
	static Array<String> extensions;
	static GLVersion glVersion;

	GL20 gl20;
	GL30 gl30;

	/** true as default. set false in order to use GL 2.0 */
	public static boolean usingGL30 = false;

	/** Default constructor */
	public LwjglGraphicsOffScreen () {
		super();
	}

	public int getHeight () {
		return 1;
	}

	public int getWidth () {
		return 1;
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
		return -1;
	}

	public float getDeltaTime () {
		return 0f;
	}

	public GraphicsType getType () {
		return GraphicsType.LWJGL;
	}

	public GLVersion getGLVersion () {
		return glVersion;
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
		return 1;
	}

	/** Only needed when setupDisplay() is not called. */
	public void initiateGL () {
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
	 * 
	 * @param enable */
	public void enableCubeMapSeamless (boolean enable) {
		if (enable) {
			gl20.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		} else {
			gl20.glDisable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		}
	}

	private void initiateGLInstances () {
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
		return true;
	}

	/** Kindly stolen from http://lwjgl.org/wiki/index.php?title=LWJGL_Basics_5_(Fullscreen), not perfect but will do. */
	@Override
	public boolean setWindowedMode (int width, int height) {
		return true;
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
	}

	/** Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take effect. */
	@Override
	public void setUndecorated (boolean undecorated) {
	}

	/** Display must be reconfigured via {@link #setWindowedMode(int, int)} for the changes to take effect. */
	@Override
	public void setResizable (boolean resizable) {
	}

	@Override
	public BufferFormat getBufferFormat () {
		return null;
	}

	@Override
	public void setVSync (boolean vsync) {
	}

	/** Sets the target framerate for the application, when using continuous rendering. Must be positive. The cpu sleeps as needed.
	 * Use 0 to never sleep. Default is 60.
	 *
	 * @param fps fps */
	@Override
	public void setForegroundFPS (int fps) {
	}

	@Override
	public boolean supportsExtension (String extension) {
		return extensions.contains(extension, false);
	}

	@Override
	public void setContinuousRendering (boolean isContinuous) {
	}

	@Override
	public boolean isContinuousRendering () {
		return true;
	}

	@Override
	public void requestRendering () {
	}

	@Override
	public boolean isFullscreen () {
		return true;
	}

	/** A callback used by LwjglApplication when trying to create the display */
	public interface SetDisplayModeCallback {
		/** If the display creation fails, this method will be called. Suggested usage is to modify the passed configuration to use
		 * a common width and height, and set fullscreen to false.
		 * 
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
	}

	@Override
	public void setSystemCursor (SystemCursor systemCursor) {
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
