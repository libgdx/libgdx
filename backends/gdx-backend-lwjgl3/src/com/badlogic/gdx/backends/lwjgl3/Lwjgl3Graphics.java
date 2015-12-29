package com.badlogic.gdx.backends.lwjgl3;

import java.awt.Toolkit;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;

public class Lwjgl3Graphics implements Graphics {
	private final Lwjgl3Window window;
	private final GL20 gl20;
	private final GL30 gl30;
	private volatile int width;
	private volatile int height;
	private boolean isFullscreen;
	private BufferFormat bufferFormat;
	private long lastFrameTime = -1;
	private float deltaTime;
	private long frameId;
	private long frameCounterStart = 0;
	private int frames;
	private int fps;		
	
	private GLFWFramebufferSizeCallback resizeCallback = new GLFWFramebufferSizeCallback() {
		@Override
		public void invoke(long windowHandle, final int width, final int height) {
			Lwjgl3Graphics.this.width = width;
			Lwjgl3Graphics.this.height = height;			
			if(!window.isListenerInitialized()) {
				return;
			}
			GLFW.glfwMakeContextCurrent(windowHandle);
			window.getListener().resize(width, height);
			window.getListener().render();
			GLFW.glfwSwapBuffers(windowHandle);			
		}
	};
	
	public Lwjgl3Graphics(Lwjgl3Window window) {
		this.window = window;
		this.gl20 = new Lwjgl3GL20();
		this.gl30 = null;
		updateFramebufferInfo();
		setupCallbacks();		
	}
	
	private void updateFramebufferInfo() {
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetFramebufferSize(window.getWindowHandle(), width, height);
		this.width = width.get(0);
		this.height = height.get(0);
		this.isFullscreen = GLFW.glfwGetWindowMonitor(window.getWindowHandle()) != 0;
		Lwjgl3ApplicationConfiguration config = window.getConfig();
		bufferFormat = new BufferFormat(config.r, config.g, config.b, config.a, config.depth, config.stencil, config.samples, false);
	}
	
	private void setupCallbacks() {
		GLFW.glfwSetFramebufferSizeCallback(window.getWindowHandle(), resizeCallback);
	}
	
	void update() {
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
		return width;
	}

	@Override
	public int getHeight() {
		return height;
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
	public float getPpiX () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpiY () {
		return Toolkit.getDefaultToolkit().getScreenResolution();
	}

	@Override
	public float getPpcX () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getPpcY () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 2.54f;
	}

	@Override
	public float getDensity () {
		return Toolkit.getDefaultToolkit().getScreenResolution() / 160f;
	}
	
	@Override
	public boolean supportsDisplayModeChange() {
		return true;
	}

	@Override
	public DisplayMode[] getDisplayModes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DisplayMode getDesktopDisplayMode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setDisplayMode(DisplayMode displayMode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setDisplayMode(int width, int height, boolean fullscreen) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTitle(String title) {
		if(title == null) {
			title = "";
		}
		GLFW.glfwSetWindowTitle(window.getWindowHandle(), title);
	}

	@Override
	public void setVSync(boolean vsync) {
		GLFW.glfwSwapInterval(vsync? 1: 0);
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
		return isFullscreen;
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
}
