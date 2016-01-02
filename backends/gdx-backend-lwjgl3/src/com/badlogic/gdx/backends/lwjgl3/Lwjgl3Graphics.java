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

import java.awt.Toolkit;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class Lwjgl3Graphics implements Graphics, Disposable {
	private final Lwjgl3Window window;
	private final GL20 gl20;
	private final GL30 gl30;
	private volatile int frameBufferWidth;
	private volatile int frameBufferHeight;
	private volatile int logicalWidth;
	private volatile int logicalHeight;
	private BufferFormat bufferFormat;
	private long lastFrameTime = -1;
	private float deltaTime;
	private long frameId;
	private long frameCounterStart = 0;
	private int frames;
	private int fps;

	IntBuffer tmpBuffer = BufferUtils.createIntBuffer(1);
	IntBuffer tmpBuffer2 = BufferUtils.createIntBuffer(1);

	private GLFWFramebufferSizeCallback resizeCallback = new GLFWFramebufferSizeCallback() {
		@Override
		public void invoke(long windowHandle, final int width, final int height) {
			Lwjgl3Graphics.this.frameBufferWidth = width;
			Lwjgl3Graphics.this.frameBufferHeight = height;
			GLFW.glfwGetWindowSize(windowHandle, tmpBuffer, tmpBuffer2);
			Lwjgl3Graphics.this.logicalWidth = tmpBuffer.get(0);
			Lwjgl3Graphics.this.logicalHeight = tmpBuffer2.get(0);
			if (!window.isListenerInitialized()) {
				return;
			}
			GLFW.glfwMakeContextCurrent(windowHandle);
			window.getListener().resize(getWidth(), getHeight());
			window.getListener().render();
			GLFW.glfwSwapBuffers(windowHandle);
		}
	};

	public Lwjgl3Graphics(Lwjgl3Window window) {
		this.window = window;
		this.gl20 = new Lwjgl3GL20();
		this.gl30 = null;
		updateFramebufferInfo();
		GLFW.glfwSetFramebufferSizeCallback(window.getWindowHandle(), resizeCallback);
	}
	
	public Lwjgl3Window getWindow() {
		return window;
	}

	private void updateFramebufferInfo() {
		GLFW.glfwGetFramebufferSize(window.getWindowHandle(), tmpBuffer, tmpBuffer2);
		this.frameBufferWidth = tmpBuffer.get(0);
		this.frameBufferHeight = tmpBuffer2.get(0);
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
	public int getWidth() {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return frameBufferWidth;
		} else {
			return logicalWidth;
		}
	}

	@Override
	public int getHeight() {
		if (window.getConfig().hdpiMode == HdpiMode.Pixels) {
			return frameBufferHeight;
		} else {
			return logicalHeight;
		}
	}

	@Override
	public int getBackBufferWidth() {
		return frameBufferWidth;
	}

	@Override
	public int getBackBufferHeight() {
		return frameBufferHeight;
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
	public float getPpiX() {
		// FIXME
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY() {
		// FIXME
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX() {
		// FIXME
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getPpcY() {
		// FIXME
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getDensity() {
		// FIXME
		return Toolkit.getDefaultToolkit().getScreenResolution() / 160f;
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
		// TODO Auto-generated method stub
		return null;
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
		return Lwjgl3ApplicationConfiguration.getDisplayModes();
	}

	@Override
	public DisplayMode[] getDisplayModes(Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayModes(monitor);
	}

	@Override
	public DisplayMode getDisplayMode() {
		return Lwjgl3ApplicationConfiguration.getDisplayMode();
	}

	@Override
	public DisplayMode getDisplayMode(Monitor monitor) {
		return Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);
	}

	@Override
	public boolean setFullscreenMode(DisplayMode displayMode) {
		window.getInput().resetPollingStates(); // need to drain all events and poll states
		return recreateWindow(0, 0, (Lwjgl3DisplayMode)displayMode);
	}

	@Override
	public boolean setWindowedMode(int width, int height) {
		window.getInput().resetPollingStates(); // need to drain all events and poll states
		if(!isFullscreen()) {					
			GLFW.glfwSetWindowSize(window.getWindowHandle(), width, height);
			return true;
		} else {
			return recreateWindow(width, height, null);
		}
	}
	
	private boolean recreateWindow(int width, int height, Lwjgl3DisplayMode displayMode) {
		Lwjgl3ApplicationConfiguration config = Lwjgl3ApplicationConfiguration.copy(window.getConfig());
		config.setWindowedMode(width, height);
		config.setFullscreenMode(displayMode);
		try {
			long oldHandle = window.getWindowHandle();
			long windowHandle = Lwjgl3Application.createGlfwWindow(config, oldHandle);
			GLFW.glfwSetFramebufferSizeCallback(oldHandle, null);
			GLFW.glfwDestroyWindow(oldHandle);
			
			GLFW.glfwSetFramebufferSizeCallback(windowHandle, resizeCallback);
			GLFW.glfwShowWindow(windowHandle);
			
			window.windowHandleChanged(windowHandle);
			return true;
		} catch(GdxRuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void setTitle(String title) {
		if (title == null) {
			title = "";
		}
		GLFW.glfwSetWindowTitle(window.getWindowHandle(), title);
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
		return GLFW.glfwExtensionSupported(extension) == GLFW.GLFW_TRUE;
	}

	@Override
	public void setContinuousRendering(boolean isContinuous) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isContinuousRendering() {
		return true;
	}

	@Override
	public void requestRendering() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isFullscreen() {
		return GLFW.glfwGetWindowMonitor(window.getWindowHandle()) != 0;
	}

	@Override
	public Cursor newCursor(Pixmap pixmap, int xHotspot, int yHotspot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCursor(Cursor cursor) {
		// TODO Auto-generated method stub
	}

	public static class Lwjgl3DisplayMode extends DisplayMode {
		final long monitor;
		
		Lwjgl3DisplayMode(long monitor, int width, int height, int refreshRate, int bitsPerPixel) {
			super(width, height, refreshRate, bitsPerPixel);
			this.monitor = monitor;
		}

		public long getMonitor() {
			return monitor;
		}
	}

	public static class Lwjgl3Monitor extends Monitor {
		final long monitor;
		Lwjgl3Monitor(long monitor, int virtualX, int virtualY, String name) {
			super(virtualX, virtualY, name);
			this.monitor = monitor;
		}
		public long getMonitor() {
			return monitor;
		}
	}

	@Override
	public void dispose() {		
		this.resizeCallback.release();
	}
}
