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

import java.nio.IntBuffer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import org.lwjgl.opengl.GL11;

public class Lwjgl3Graphics implements Graphics, Disposable {
	private final Lwjgl3Window window;
	private GL20 gl20;
	private GL30 gl30;
	private GLVersion glVersion;
	private volatile int backBufferWidth;
	private volatile int backBufferHeight;
	private volatile int logicalWidth;
	private volatile int logicalHeight;
	private volatile boolean isContinuous = true;
	private BufferFormat bufferFormat;
	private long lastFrameTime = -1;
	private float deltaTime;
	private long frameId;
	private long frameCounterStart = 0;
	private int frames;
	private int fps;
	private int windowPosXBeforeFullscreen;
	private int windowPosYBeforeFullscreen;
	private DisplayMode displayModeBeforeFullscreen = null;

	IntBuffer tmpBuffer = BufferUtils.createIntBuffer(1);
	IntBuffer tmpBuffer2 = BufferUtils.createIntBuffer(1);

	private GLFWFramebufferSizeCallback resizeCallback = new GLFWFramebufferSizeCallback() {
		@Override
		public void invoke(long windowHandle, final int width, final int height) {
			updateFramebufferInfo();
			if (!window.isListenerInitialized()) {
				return;
			}
			window.makeCurrent();
			gl20.glViewport(0, 0, width, height);
			window.getListener().resize(getWidth(), getHeight());
			window.getListener().render();
			GLFW.glfwSwapBuffers(windowHandle);
		}
	};

	public Lwjgl3Graphics(Lwjgl3Window window) {
		this.window = window;
		if (window.getConfig().useGL30) {
			this.gl30 = new Lwjgl3GL30();
			this.gl20 = this.gl30;
		} else {
			this.gl20 = new Lwjgl3GL20();
			this.gl30 = null;
		}
		updateFramebufferInfo();
		initiateGL();
		GLFW.glfwSetFramebufferSizeCallback(window.getWindowHandle(), resizeCallback);
	}

	private void initiateGL () {
		String versionString = gl20.glGetString(GL11.GL_VERSION);
		String vendorString = gl20.glGetString(GL11.GL_VENDOR);
		String rendererString = gl20.glGetString(GL11.GL_RENDERER);
		glVersion = new GLVersion(Application.ApplicationType.Desktop, versionString, vendorString, rendererString);
	}

	public Lwjgl3Window getWindow() {
		return window;
	}

	private void updateFramebufferInfo() {
		GLFW.glfwGetFramebufferSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		this.backBufferWidth = tmpBuffer.get(0);
		this.backBufferHeight = tmpBuffer2.get(0);
		GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		Lwjgl3Graphics.this.logicalWidth = tmpBuffer.get(0);
		Lwjgl3Graphics.this.logicalHeight = tmpBuffer2.get(0);
		Lwjgl3ApplicationConfiguration config = window.getConfig();
		bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil,
				config.samples, false);
	}

	void update() {
		long time = System.nanoTime();
		if (lastFrameTime == -1)
			lastFrameTime = time;
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
	public boolean isGL30Available() {
		return gl30 != null;
	}

	@Override
	public GL20 getGL20() {
		return gl20;
	}

	@Override
	public GL30 getGL30() {
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
	public int getWidth() {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return backBufferWidth;
		} else {
			return logicalWidth;
		}
	}

	@Override
	public int getHeight() {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return backBufferHeight;
		} else {
			return logicalHeight;
		}
	}

	@Override
	public int getBackBufferWidth() {
		return backBufferWidth;
	}

	@Override
	public int getBackBufferHeight() {
		return backBufferHeight;
	}

	public int getLogicalWidth() {
		return logicalWidth;
	}

	public int getLogicalHeight() {
		return logicalHeight;
	}

	@Override
	public long getFrameId() {
		return frameId;
	}

	@Override
	public float getDeltaTime() {
		return deltaTime;
	}

	@Override
	public float getRawDeltaTime() {
		return deltaTime;
	}

	@Override
	public int getFramesPerSecond() {
		return fps;
	}

	@Override
	public GraphicsType getType() {
		return GraphicsType.LWJGL3;
	}

	@Override
	public GLVersion getGLVersion () {
		return glVersion;
	}

	@Override
	public float getPpiX() {
		return getPpcX() / 0.393701f;
	}

	@Override
	public float getPpiY() {
		return getPpcY() / 0.393701f;
	}

	@Override
	public float getPpcX() {
		Lwjgl3Monitor monitor = (Lwjgl3Monitor) getMonitor();
		GLFW.glfwGetMonitorPhysicalSize(monitor.monitorHandle, tmpBuffer, tmpBuffer2);
		int sizeX = tmpBuffer.get(0);
		DisplayMode mode = getDisplayMode();
		return mode.width / (float) sizeX * 10;
	}

	@Override
	public float getPpcY() {
		Lwjgl3Monitor monitor = (Lwjgl3Monitor) getMonitor();
		GLFW.glfwGetMonitorPhysicalSize(monitor.monitorHandle, tmpBuffer, tmpBuffer2);
		int sizeY = tmpBuffer2.get(0);
		DisplayMode mode = getDisplayMode();
		return mode.height / (float) sizeY * 10;
	}

	@Override
	public float getDensity() {
		return getPpiX() / 160f;
	}

	@Override
	public boolean supportsDisplayModeChange() {
		return true;
	}

	@Override
	public Monitor getPrimaryMonitor() {
		return Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(GLFW.glfwGetPrimaryMonitor());
	}

	@Override
	public Monitor getMonitor() {
		Monitor[] monitors = getMonitors();
		Monitor result = monitors[0];

		GLFW.glfwGetWindowPos(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		int windowX = tmpBuffer.get(0);
		int windowY = tmpBuffer2.get(0);
		GLFW.glfwGetWindowSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		int windowWidth = tmpBuffer.get(0);
		int windowHeight = tmpBuffer2.get(0);
		int overlap;
		int bestOverlap = 0;

		for (Monitor monitor : monitors) {
			DisplayMode mode = getDisplayMode(monitor);

			overlap = Math.max(0,
					Math.min(windowX + windowWidth, monitor.virtualX + mode.width)
							- Math.max(windowX, monitor.virtualX))
					* Math.max(0, Math.min(windowY + windowHeight, monitor.virtualY + mode.height)
							- Math.max(windowY, monitor.virtualY));

			if (bestOverlap < overlap) {
				bestOverlap = overlap;
				result = monitor;
			}
		}
		return result;
	}

	@Override
	public Monitor[] getMonitors() {
		PointerBuffer glfwMonitors = GLFW.glfwGetMonitors();
		Monitor[] monitors = new Monitor[glfwMonitors.limit()];
		for (int i = 0; i < glfwMonitors.limit(); i++) {
			monitors[i] = Lwjgl3ApplicationConfiguration.toLwjgl3Monitor(glfwMonitors.get(i));
		}
		return monitors;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		return Lwjgl3ApplicationConfiguration.getDisplayModes(getMonitor());
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayModes(monitor);
	}

	@Override
	public DisplayMode getDisplayMode() {
		return Lwjgl3ApplicationConfiguration.getDisplayMode(getMonitor());
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		window.getInput().resetPollingStates();
		Lwjgl3DisplayMode newMode = (Lwjgl3DisplayMode) displayMode;
		if (isFullscreen()) {
			Lwjgl3DisplayMode currentMode = (Lwjgl3DisplayMode) getDisplayMode();
			if (currentMode.getMonitor() == newMode.getMonitor() && currentMode.refreshRate == newMode.refreshRate) {
				// same monitor and refresh rate
				GLFW.glfwSetWindowSize(window.getWindowHandle(), newMode.width, newMode.height);
			} else {
				// different monitor and/or refresh rate
				GLFW.glfwSetWindowMonitor(window.getWindowHandle(), newMode.getMonitor(),
						0, 0, newMode.width, newMode.height, newMode.refreshRate);
			}
		} else {
			// store window position so we can restore it when switching from fullscreen to windowed later
			storeCurrentWindowPositionAndDisplayMode();
			
			// switch from windowed to fullscreen
			GLFW.glfwSetWindowMonitor(window.getWindowHandle(), newMode.getMonitor(),
					0, 0, newMode.width, newMode.height, newMode.refreshRate);
		}
		updateFramebufferInfo();
		return true;
	}
	
	private void storeCurrentWindowPositionAndDisplayMode() {
		windowPosXBeforeFullscreen = window.getPositionX();
		windowPosYBeforeFullscreen = window.getPositionY();
		displayModeBeforeFullscreen = getDisplayMode();
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		window.getInput().resetPollingStates();
		if (!isFullscreen()) {
			GLFW.glfwSetWindowSize(window.getWindowHandle(), width, height);
		} else {
			if (displayModeBeforeFullscreen == null) {
				storeCurrentWindowPositionAndDisplayMode();
			}
				
			GLFW.glfwSetWindowMonitor(window.getWindowHandle(), 0,
					windowPosXBeforeFullscreen, windowPosYBeforeFullscreen, width, height,
					displayModeBeforeFullscreen.refreshRate);
		}
		updateFramebufferInfo();
		return true;
	}

	@Override
	public void setTitle(String title) {
		if (title == null) {
			title = "";
		}
		GLFW.glfwSetWindowTitle(window.getWindowHandle(), title);
	}

	@Override
	public void setUndecorated(boolean undecorated) {
		Lwjgl3ApplicationConfiguration config = getWindow().getConfig();
		config.setDecorated(!undecorated);
		GLFW.glfwSetWindowAttrib(window.getWindowHandle(), GLFW.GLFW_DECORATED, undecorated ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);
	}

	@Override
	public void setResizable(boolean resizable) {
		Lwjgl3ApplicationConfiguration config = getWindow().getConfig();
		config.setResizable(resizable);
		GLFW.glfwSetWindowAttrib(window.getWindowHandle(), GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
	}

	@Override
	public void setVSync(boolean vsync) {
		GLFW.glfwSwapInterval(vsync ? 1 : 0);
	}

	@Override
	public BufferFormat getBufferFormat() {
		return bufferFormat;
	}

	@Override
	public boolean supportsExtension(String extension) {
		return GLFW.glfwExtensionSupported(extension);
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
		this.isContinuous = isContinuous;
	}

	@Override
	public boolean isContinuousRendering() {
		return isContinuous;
	}

	@Override
	public void requestRendering() {
		window.requestRendering();
	}

	@Override
	public boolean isFullscreen() {
		return GLFW.glfwGetWindowMonitor(window.getWindowHandle()) != 0;
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		return new Lwjgl3Cursor(getWindow(), pixmap, xHotspot, yHotspot);
	}

	@Override
	public void setCursor(Cursor cursor) {
		GLFW.glfwSetCursor(getWindow().getWindowHandle(), ((Lwjgl3Cursor) cursor).glfwCursor);
	}

	@Override
	public void setSystemCursor(SystemCursor systemCursor) {
		Lwjgl3Cursor.setSystemCursor(getWindow().getWindowHandle(), systemCursor);
	}

	@Override
	public void dispose() {
		this.resizeCallback.free();
	}

	public static class Lwjgl3DisplayMode extends DisplayMode {
		final long monitorHandle;

		Lwjgl3DisplayMode(long monitor, int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
			this.monitorHandle = monitor;
		}

		public long getMonitor() {
			return monitorHandle;
		}
	}

	public static class Lwjgl3Monitor extends Monitor {
		final long monitorHandle;

		Lwjgl3Monitor(long monitor, int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
			this.monitorHandle = monitor;
		}

		public long getMonitorHandle() {
			return monitorHandle;
		}
	}
}
