package com.badlogic.gdx.backends.lwjgl3;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

import org.lwjgl.glfw.GLFW;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.utils.Disposable;

class Lwjgl3Window implements Disposable {
	private long windowHandle;
	private final ApplicationListener listener;
	private final Lwjgl3Graphics graphics;
	private final Lwjgl3Input input;
	private final Lwjgl3ApplicationConfiguration config;

	public Lwjgl3Window(long windowHandle, ApplicationListener listener,
			Lwjgl3ApplicationConfiguration config) {
		this.windowHandle = windowHandle;
		this.listener = listener;
		this.graphics = new Lwjgl3Graphics(this);
		this.input = new Lwjgl3Input(this);
		this.config = config;
	}

	public ApplicationListener getListener() {
		return listener;
	}

	public Lwjgl3Graphics getGraphics() {
		return graphics;
	}

	public Lwjgl3Input getInput() {
		return input;
	}

	public long getWindowHandle() {
		return windowHandle;
	}
	
	void windowHandleChanged(long windowHandle) {
		this.windowHandle = windowHandle;
		input.windowHandleChanged(windowHandle);
	}
	
	public void update() {
		glfwSwapBuffers(windowHandle);
		glfwPollEvents();
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(windowHandle) == GLFW.GLFW_TRUE;
	}

	@Override
	public void dispose() {
		listener.pause();
		listener.dispose();
	}
}
