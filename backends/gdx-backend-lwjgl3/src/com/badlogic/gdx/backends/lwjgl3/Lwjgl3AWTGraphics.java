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

import com.badlogic.gdx.AbstractGraphics;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.utils.Disposable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.swing.SwingUtilities;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.nio.IntBuffer;

public class Lwjgl3AWTGraphics extends AbstractGraphics implements Disposable {
	final Lwjgl3AWTCanvas window;
	GL20 gl20;
	private GL30 gl30;
	private GLVersion glVersion;
	private volatile int backBufferWidth;
	private volatile int backBufferHeight;
	private volatile int logicalWidth;
	private volatile int logicalHeight;
	volatile boolean isContinuous = true;
	volatile boolean requestRendering = false;
	private BufferFormat bufferFormat;
	private long lastFrameTime = -1;
	private float deltaTime;
	private long frameId;
	private long frameCounterStart = 0;
	private int frames;
	private int fps;

	IntBuffer tmpBuffer = BufferUtils.createIntBuffer(1);
	IntBuffer tmpBuffer2 = BufferUtils.createIntBuffer(1);

	public Lwjgl3AWTGraphics (Lwjgl3AWTCanvas window) {
		this.window = window;
		if (window.getConfig().useGL30) {
			this.gl30 = new Lwjgl3GL30();
			this.gl20 = this.gl30;
		} else {
			this.gl20 = new Lwjgl3GL20();
			this.gl30 = null;
		}

		updateFramebufferInfo();

		window.getCanvas().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized (ComponentEvent e) {
				updateViewport();
			}

			@Override
			public void componentMoved (ComponentEvent e) {
				updateViewport();
			}
		});
	}

	private void updateViewport () {
		updateFramebufferInfo();
		window.getCanvas().runInContext( () -> gl20.glViewport(0, 0, getWidth(), getHeight()));
	}

	void initiateGL () {
		String versionString = gl20.glGetString(GL11.GL_VERSION);
		String vendorString = gl20.glGetString(GL11.GL_VENDOR);
		String rendererString = gl20.glGetString(GL11.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	public Lwjgl3AWTCanvas getWindow () {
		return window;
	}

	void updateFramebufferInfo () {
		this.backBufferWidth = getWindow().getCanvas().getWidth();
		this.backBufferHeight = getWindow().getCanvas().getHeight();
		Lwjgl3AWTGraphics.this.logicalWidth = getWindow().getCanvas().getWidth();
		Lwjgl3AWTGraphics.this.logicalHeight = getWindow().getCanvas().getHeight();
		Lwjgl3ApplicationConfiguration config = window.getConfig();
		bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples,
			false);
	}

	void update () {
		long time = System.nanoTime();
		if (lastFrameTime == -1) lastFrameTime = time;
		deltaTime = (time - lastFrameTime) / 1000000000.0f;
		lastFrameTime = time;

		if (time - frameCounterStart >= 1000000000) {
			fps = frames;
			frames = 0;
			frameCounterStart = time;
		}
		frames++;
		frameId++;
	}

	@Override
	public boolean isGL30Available () {
		return gl30 != null;
	}

	@Override
	public GL20 getGL20 () {
		return gl20;
	}

	@Override
	public GL30 getGL30 () {
		return gl30;
	}

	@Override
	public void setGL20 (GL20 gl20) {
		this.gl20 = gl20;
	}

	@Override
	public void setGL30 (GL30 gl30) {
		this.gl30 = gl30;
	}

	@Override
	public int getWidth () {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return backBufferWidth;
		} else {
			return logicalWidth;
		}
	}

	@Override
	public int getHeight () {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return backBufferHeight;
		} else {
			return logicalHeight;
		}
	}

	@Override
	public int getBackBufferWidth () {
		return backBufferWidth;
	}

	@Override
	public int getBackBufferHeight () {
		return backBufferHeight;
	}

	public int getLogicalWidth () {
		return logicalWidth;
	}

	public int getLogicalHeight () {
		return logicalHeight;
	}

	@Override
	public long getFrameId () {
		return frameId;
	}

	@Override
	public float getDeltaTime () {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond () {
		return fps;
	}

	@Override
	public GraphicsType getType () {
		return GraphicsType.LWJGL3;
	}

	@Override
	public GLVersion getGLVersion () {
		return glVersion;
	}

	@Override
	public float getPpiX () {
		return getPpcX() / 0.393701f;
	}

	@Override
	public float getPpiY () {
		return getPpcY() / 0.393701f;
	}

	@Override
	public float getPpcX () {
		Lwjgl3Monitor monitor = (Lwjgl3Monitor)getMonitor();
		GLFW.glfwGetMonitorPhysicalSize(monitor.monitorHandle, tmpBuffer, tmpBuffer2);
		int sizeX = tmpBuffer.get(0);
		DisplayMode mode = getDisplayMode();
		return mode.width / (float)sizeX * 10;
	}

	@Override
	public float getPpcY () {
		Lwjgl3Monitor monitor = (Lwjgl3Monitor)getMonitor();
		GLFW.glfwGetMonitorPhysicalSize(monitor.monitorHandle, tmpBuffer, tmpBuffer2);
		int sizeY = tmpBuffer2.get(0);
		DisplayMode mode = getDisplayMode();
		return mode.height / (float)sizeY * 10;
	}

	@Override
	public boolean supportsDisplayModeChange () {
		return true;
	}

	@Override
	public Monitor getPrimaryMonitor () {
		return Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor());
	}

	@Override
	public Monitor getMonitor () {
		Monitor[] monitors = getMonitors();

		return monitors[0];
	}

	@Override
	public Monitor[] getMonitors () {
		PointerBuffer glfwMonitors = GLFW.glfwGetMonitors();
		Monitor[] monitors = new Monitor[glfwMonitors.limit()];
		for (int i = 0; i < glfwMonitors.limit(); i++) {
			monitors[i] = Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(glfwMonitors.get(i));
		}
		return monitors;
	}

	@Override
	public DisplayMode[] getDisplayModes () {
		return Lwjgl3ApplicationConfiguration.getDisplayModes(getMonitor());
	}

	@Override
	public DisplayMode[] getDisplayModes (Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayModes(monitor);
	}

	@Override
	public DisplayMode getDisplayMode () {
		return Lwjgl3ApplicationConfiguration.getDisplayMode(getMonitor());
	}

	@Override
	public DisplayMode getDisplayMode (Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);
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
		return false;
	}

	@Override
	public boolean setWindowedMode (int width, int height) {
		Window window = SwingUtilities.getWindowAncestor(getWindow().canvas);
		// TODO: better set the size of the canvas?
		window.setSize(width, height);
		return true;
	}

	@Override
	public void setTitle (String title) {
		if (title == null) {
			title = "";
		}
		Window window = SwingUtilities.getWindowAncestor(getWindow().canvas);
		if (window instanceof Frame) {
			((Frame)window).setTitle(title);
		} else if (window instanceof Dialog) {
			((Dialog)window).setTitle(title);
		}
	}

	@Override
	public void setUndecorated (boolean undecorated) {
		Lwjgl3ApplicationConfiguration config = getWindow().getConfig();
		config.setDecorated(!undecorated);
		Window window = SwingUtilities.getWindowAncestor(getWindow().canvas);
		if (window instanceof Frame) {
			((Frame)window).setUndecorated(undecorated);
		} else if (window instanceof Dialog) {
			((Dialog)window).setUndecorated(undecorated);
		}
	}

	@Override
	public void setResizable (boolean resizable) {
		Lwjgl3ApplicationConfiguration config = getWindow().getConfig();
		config.setResizable(resizable);
		Window window = SwingUtilities.getWindowAncestor(getWindow().canvas);
		if (window instanceof Frame) {
			((Frame)window).setResizable(resizable);
		} else if (window instanceof Dialog) {
			((Dialog)window).setResizable(resizable);
		}
	}

	@Override
	public void setVSync (boolean vsync) {
		window.getConfig().vSyncEnabled = vsync;

		GLFW.glfwSwapInterval(vsync ? 1 : 0);
	}

	@Override
	public BufferFormat getBufferFormat () {
		return bufferFormat;
	}

	/** Sets the target framerate for the application, when using continuous rendering. Must be positive.
	 * The cpu sleeps as needed. Use 0 to never sleep. Default is 60.
	 *
	 * @param fps fps */
	@Override
	public void setForegroundFPS (int fps) {
		this.window.config.foregroundFPS = fps;
	}

	@Override
	public boolean supportsExtension (String extension) {
		return GLFW.glfwExtensionSupported(extension);
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
			return rq || isContinuous;
		}
	}

	@Override
	public boolean isFullscreen () {
// return GLFW.glfwGetWindowMonitor(window.getWindowHandle()) != 0;
		return false;
	}

	@Override
	public Cursor newCursor (Pixmap pixmap, int xHotspot, int yHotspot) {
// return new Lwjgl3Cursor(getWindow(), pixmap, xHotspot, yHotspot);
		return null;
	}

	@Override
	public void setCursor (Cursor cursor) {
// GLFW.glfwSetCursor(getWindow().getWindowHandle(), ((Lwjgl3Cursor) cursor).glfwCursor);
	}

	@Override
	public void setSystemCursor (SystemCursor systemCursor) {
// Lwjgl3Cursor.setSystemCursor(getWindow().getWindowHandle(), systemCursor);
	}

	@Override
	public void dispose () {
// this.resizeCallback.free();
	}
}
